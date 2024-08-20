import pytest
from sim._tsdz2 import ffi, lib as ebike # module generated from c-code


# Set up initial values before each test
@pytest.fixture(autouse=True)
def setup_ebike():
    # Set up initial values before each test
    ebike.m_configuration_variables.ui16_wheel_perimeter = 2070
    yield
    # Teardown after each test (optional)
    ebike.ui16_wheel_speed_sensor_ticks = 0
    ebike.ui16_wheel_speed_x10 = 0


# Test function for calc_wheel_speed
def test_calc_wheel_speed_simple():
    ebike.ui16_wheel_speed_sensor_ticks = 10000
    ebike.calc_wheel_speed()
    result = ebike.ui16_wheel_speed_x10
    expected = 141
    assert result == expected, f'Test failed! Expected {expected} value, got {result} value'

# Parameterized test function with different ticks values
@pytest.mark.parametrize("ticks, expected", [
    # (0,     0),
    (1,     39624),
    (1000,  1415),
    (2000,  707),
    (5000,  283),
    (10000, 141),
    (20000, 70),
    (65535, 21)
])
def test_calc_wheel_speed_with_various_ticks(ticks, expected):
    ebike.ui16_wheel_speed_sensor_ticks = ticks
    ebike.calc_wheel_speed()
    result = ebike.ui16_wheel_speed_x10
    assert result == expected, f'Expected {expected/10}km/h, got {result/10}km/h'





MOTOR_TASK_FREQ = 16000000 / (420*2)
def wheel_speed_calc_float(ui16_wheel_perimeter: int, ui16_wheel_speed_sensor_ticks: int) -> float:
    rps = MOTOR_TASK_FREQ / ui16_wheel_speed_sensor_ticks
    kph = rps * ui16_wheel_perimeter * ((3600 / (1000 * 1000)))
    return kph

def wheel_inch_to_mm_circumference(wheel_inch: float) -> float:
    diameter_mm = wheel_inch * 25.4
    circumference_mm = diameter_mm * 3.14159
    return circumference_mm


@pytest.mark.parametrize("wheel_size", range(14, 30))
def test_wheel_speed_calculation_precision_parametrized(wheel_size):
    """
    Test wheel speed calculation precision for different wheel sizes
    
    :param ebike: An instance of the ebike with necessary attributes and methods
    :param wheel_size: The wheel perimeter to test
    """
    ui16_wheel_perimeter = int(wheel_inch_to_mm_circumference(wheel_size)) 
    ebike.m_configuration_variables.ui16_wheel_perimeter = ui16_wheel_perimeter
    error = {}
    for ticks in range(1000, 65535, 100):     # Range from 1 to 10000
        ebike.ui16_wheel_speed_sensor_ticks = ticks
        ebike.calc_wheel_speed()
        result = ebike.ui16_wheel_speed_x10 / 10
        expected = wheel_speed_calc_float(ui16_wheel_perimeter, ticks)
        assert result == pytest.approx(expected, rel=0.1, abs=0.1), (
            f"Test failed for wheel size {wheel_size} (perimeter {ui16_wheel_perimeter}) and tick count {ticks}! "
            f"Expected {expected:.2f}, got {result:.2f}"
        )
        error[ticks] = abs(result - expected)
    # pytest -s to print
    print(f"Biggest error: {max(error.values())}")
    print(f"Average error: {sum(error.values()) / len(error)}")


# Run the tests
if __name__ == '__main__':
    pytest.main()
