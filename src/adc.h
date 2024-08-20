/*
 * TongSheng TSDZ2 motor controller firmware/
 *
 * Copyright (C) Casainho and Leon, 2019.
 *
 * Released under the GPL License, Version 3
 */

#ifndef ADC_H_
#define ADC_H_


// for AIN6: 0x53E0 + 2*6 = 0x53E8
//#define UI8_ADC_BATTERY_VOLTAGE             (*(uint8_t*)(0x53EC)) // AIN6
//#define UI8_ADC_THROTTLE                    (*(uint8_t*)(0x53EE)) // AIN7
//#define UI8_ADC_BATTERY_CURRENT             (*(uint8_t*)(0x53EA)) // AIN5
//#define UI8_ADC_TORQUE_SENSOR               (*(uint8_t*)(0x53E8)) // AIN4

//                                                        MSB                          LSB
//#define UI16_ADC_10_BIT_BATTERY_VOLTAGE      (((*(uint8_t*)(0x53EC)) << 2) | (*(uint8_t*)(0x53ED)))
//#define UI16_ADC_10_BIT_BATTERY_CURRENT      (((*(uint8_t*)(0x53EA)) << 2) | (*(uint8_t*)(0x53EB)))
//#define UI16_ADC_10_BIT_THROTTLE             (((*(uint8_t*)(0x53EE)) << 2) | (*(uint8_t*)(0x53EF)))
//#define UI16_ADC_10_BIT_TORQUE_SENSOR        (((*(uint8_t*)(0x53E8)) << 2) | (*(uint8_t*)(0x53E9)))


void adc_init(void);
#endif /* _ADC_H */
