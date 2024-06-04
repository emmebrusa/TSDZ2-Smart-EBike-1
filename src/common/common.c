/*
 * TongSheng TSDZ2 motor controller firmware/
 *
 * Copyright (C) Casainho, Leon, MSpider65 2020.
 *
 * Released under the GPL License, Version 3
 */

#include <stdint.h>
#include "stm8s.h"
#include "common.h"

uint16_t map_ui16(uint16_t in, uint16_t in_min, uint16_t in_max, uint16_t out_min, uint16_t out_max) {
    // if input min is smaller than output min, return the output min value
    uint16_t out;
    if (in <= in_min) {
        out = out_min;
    }
    // if input max is bigger than output max, return the output max value
    else if (in >= in_max) {
        out = out_max;
    }else{
        uint16_t in_range = in_max - in_min;
        uint16_t out_range;
        if (out_max > out_min) {//if output is inverted
            out_range = out_max - out_min;
        }else{
            out_range = out_min - out_max;
        }
        // map the input to the output range, round up if mapping bigger ranges to smaller ranges
        if (in_range > out_range) {
            out =  (uint16_t)(((uint32_t)(in - in_min) * (out_range + 1U)) / (in_range + 1U)) + out_min;
        }
        // map the input to the output range, round down if mapping smaller ranges to bigger ranges
        else {
            out = (uint16_t)(((uint32_t)(in - in_min) * out_range) / in_range) + out_min;
        }
    }
    return out;
}

uint8_t map_ui8(uint8_t in, uint8_t in_min, uint8_t in_max, uint8_t out_min, uint8_t out_max) {
    // if input min is smaller than output min, return the output min value
    if (in <= in_min) {
        return out_min;
    }

    // if input max is bigger than output max, return the output max value
    if (in >= in_max) {
        return out_max;
    }

    if (out_max < out_min)
        return (uint16_t)out_min - (uint16_t)((uint8_t)(in - in_min) * (uint8_t)(out_min - out_max)) / (uint8_t)(in_max - in_min);
    else
        return (uint16_t)out_min + (uint16_t)((uint8_t)(in - in_min) * (uint8_t)(out_max - out_min)) / (uint8_t)(in_max - in_min);
}

uint8_t ui8_min(uint8_t value_a, uint8_t value_b) {
    if (value_a < value_b) {
        return value_a;
    } else {
        return value_b;
    }
}

uint8_t ui8_max(uint8_t value_a, uint8_t value_b) {
    if (value_a > value_b)
        return value_a;
    else
        return value_b;
}

uint16_t filter(uint16_t ui16_new_value, uint16_t ui16_old_value, uint8_t ui8_alpha) {
    if (ui8_alpha < 11) {
        uint32_t ui32_temp_new = (uint32_t) ui16_new_value * (uint32_t)(10U - ui8_alpha);
		uint32_t ui32_temp_old = (uint32_t) ui16_old_value * (uint32_t) ui8_alpha;
        uint16_t ui16_filtered_value = (uint16_t)((ui32_temp_new + ui32_temp_old + 5U) / 10U);

        if (ui16_filtered_value == ui16_old_value) {
            if (ui16_filtered_value < ui16_new_value)
				ui16_filtered_value++;
			else if (ui16_filtered_value > ui16_new_value)
				ui16_filtered_value--;
        }

        return ui16_filtered_value;
    } else {
        return 0;
    }
}

// from here: https://github.com/FxDev/PetitModbus/blob/master/PetitModbus.c
/*
 * Function Name        : CRC16
 * @param[in]           : ui8_data  - Data to Calculate CRC
 * @param[in/out]       : ui16_crc   - Anlik CRC degeri
 * @How to use          : First initial data has to be 0xFFFF.
 */
void crc16(uint8_t ui8_data, uint16_t *ui16_crc) {
    unsigned int i;

    *ui16_crc = *ui16_crc ^ (uint16_t) ui8_data;

    for (i = 8; i > 0; i--) {
        if (*ui16_crc & 0x0001) {
            *ui16_crc = (*ui16_crc >> 1) ^ 0xA001;
        } else {
            *ui16_crc >>= 1;
        }
    }
}
