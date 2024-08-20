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

def test_battery_current_max_from_battery_power_max():
    '''Test function for the battery current maximum calculation based on the "battery power max" limits from config.h'''
# Test function for calc_wheel_speed
    ebike.m_configuration_variables.ui8_battery_current_max = 255 # maxout the other limit so that battery limit is based on the battery power
    ebike.ui16_battery_voltage_filtered_x1000 = 48*1000
    ebike.ebike_app_init()
    ebike.uart_receive_package()
    result = ebike.ui8_adc_battery_current_max
    expected = 112
    assert result == expected


# Run the tests
if __name__ == '__main__':
    pytest.main()
