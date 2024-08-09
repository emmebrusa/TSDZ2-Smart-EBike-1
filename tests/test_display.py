import pytest
from sim._tsdz2 import ffi, lib as ebike # module generated from c-code

# Mock UART
class UART:
    message = bytearray(b"\x59\x40\x00\x1C\x00\x1B\xD0")
    index = 0

    @staticmethod
    @ffi.def_extern()
    def UART2_GetFlagStatus(flag_key):
        if flag_key in [ebike.UART2_FLAG_RXNE, ebike.UART2_FLAG_TXE]:
            return 1  # ready
        return 0  # unhandled

    @staticmethod
    @ffi.def_extern()
    def UART2_ReceiveData8():
        byte = UART.message[UART.index]
        UART.index = (UART.index + 1) % len(UART.message)
        return byte

# Test the receive function
def test_receive():
    assert ebike.uart_get_char() == 0x59
    assert ebike.uart_get_char() == 0x40
    assert ebike.uart_get_char() == 0x00
    assert ebike.uart_get_char() == 0x1C
    assert ebike.uart_get_char() == 0x00
    assert ebike.uart_get_char() == 0x1B
    assert ebike.uart_get_char() == 0xD0
    assert ebike.uart_get_char() == 0x59

# Run the tests
if __name__ == '__main__':
    pytest.main()
