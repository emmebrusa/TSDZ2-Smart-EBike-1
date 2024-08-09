/*
 * TongSheng TSDZ2 motor controller firmware/
 *
 * Copyright (C) Casainho, Leon, MSpider65 2020.
 *
 * Released under the GPL License, Version 3
 */

#ifndef _MOTOR_H_
#define _MOTOR_H_

#include <stdint.h>

// motor states
#define BLOCK_COMMUTATION 			            0
#define SINEWAVE_INTERPOLATION_60_DEGREES 	    0x80

// power variables
extern volatile uint8_t ui8_controller_duty_cycle_ramp_up_inverse_step;
extern volatile uint8_t ui8_controller_duty_cycle_ramp_down_inverse_step;
extern volatile uint16_t ui16_adc_voltage_cut_off;
extern volatile uint8_t ui8_adc_battery_current_filtered;
extern volatile uint8_t ui8_controller_adc_battery_current_target;
extern volatile uint8_t ui8_g_duty_cycle;
extern volatile uint8_t ui8_fw_hall_counter_offset;
extern volatile uint8_t ui8_fw_hall_counter_offset_max;
extern volatile uint8_t ui8_field_weakening_enabled;
extern volatile uint16_t ui16_hall_counter_total;
extern volatile uint8_t ui8_controller_duty_cycle_target;
extern volatile uint16_t ui16_hall_calib_cnt[6];
extern volatile uint8_t ui8_hall_ref_angles[6];
extern volatile uint8_t ui8_hall_counter_offsets[6];

// Sensors
extern volatile uint8_t ui8_brake_state;
extern volatile uint16_t ui16_adc_voltage;
extern volatile uint16_t ui16_adc_torque;
extern volatile uint16_t ui16_adc_throttle;

// cadence sensor
extern volatile uint16_t ui16_cadence_sensor_ticks;

// wheel speed sensor
extern volatile uint16_t ui16_wheel_speed_sensor_ticks;

// battery soc
extern volatile uint8_t ui8_battery_SOC_saved_flag;
extern volatile uint8_t ui8_battery_SOC_reset_flag;

void hall_sensor_init(void); // must be called before using the motor
void motor_enable_pwm(void);
void motor_disable_pwm(void);

#endif /* _MOTOR_H_ */
