/*
 * TongSheng TSDZ2 motor controller firmware
 *
 * Copyright (C) Casainho, Leon, MSpider65 2020.
 *
 * Released under the GPL License, Version 3
 */

#include "ebike_app.h"
#include <stdint.h>
#include "stm8s.h"
#include "stm8s_uart2.h"
#include "stm8s_gpio.h"
#include "main.h"
#include "interrupts.h"
#include "adc.h"
#include "motor.h"
#include "pwm.h"
#include "uart.h"
#include "brake.h"
#include "eeprom.h"
#include "lights.h"
#include "common.h"

volatile struct_configuration_variables m_configuration_variables;

// display menu
static uint8_t ui8_assist_level = ECO;
static uint8_t ui8_assist_level_temp = ECO;
static uint8_t ui8_assist_level_01_flag = 0;
static uint8_t ui8_walk_assist_flag = 0;
static uint8_t ui8_riding_mode_temp = 0;
static uint8_t ui8_lights_flag = 0;
static uint8_t ui8_lights_on_5s = 0;
static uint8_t ui8_menu_flag = 0;
static uint8_t ui8_menu_index = 0;
static uint8_t ui8_data_index = 0;
static uint8_t ui8_lights_counter = 0;
static uint8_t ui8_menu_counter = 0;
static uint8_t ui8_display_function_code = 0;
static uint8_t ui8_display_function_code_temp = 0;
static uint8_t ui8_menu_function_enabled = 0;
static uint8_t ui8_display_data_enabled = 0;
static uint16_t ui16_display_data = 0;
static uint8_t ui8_auto_display_data_flag = 0;
static uint8_t ui8_auto_display_data_status = 0;
static uint16_t ui16_display_data_factor = 0;
static uint8_t ui8_display_blinking_code = 0;
static uint8_t ui8_delay_display_function = DELAY_MENU_ON;
static uint8_t ui8_display_data_on_startup = DATA_DISPLAY_ON_STARTUP;
static uint8_t ui8_set_parameter_enabled_temp = ENABLE_SET_PARAMETER_ON_STARTUP;
static uint8_t ui8_auto_display_data_enabled_temp = ENABLE_AUTO_DATA_DISPLAY;
static uint8_t ui8_street_mode_enabled_temp = ENABLE_STREET_MODE_ON_STARTUP;
static uint8_t ui8_torque_sensor_adv_enabled_temp = TORQUE_SENSOR_ADV_ON_STARTUP;
static uint8_t ui8_assist_without_pedal_rotation_temp = MOTOR_ASSISTANCE_WITHOUT_PEDAL_ROTATION;
static uint8_t ui8_street_mode_walk_assist_enabled = STREET_MODE_WALK_ENABLED;
static uint8_t ui8_display_battery_soc = 0;
static uint8_t ui8_display_riding_mode = 0;
static uint8_t ui8_display_lights_configuration = 0;
static uint8_t ui8_display_alternative_lights_configuration = 0;
static uint8_t ui8_display_torque_sensor_flag_1 = 0;
static uint8_t ui8_display_torque_sensor_flag_2 = 0;
static uint8_t ui8_display_torque_sensor_value = 0;
static uint8_t ui8_display_torque_sensor_step = 0;
static uint8_t ui8_display_function_status[3][5];
static uint8_t ui8_lights_configuration_2 = LIGHTS_CONFIGURATION_2;
static uint8_t ui8_lights_configuration_3 = LIGHTS_CONFIGURATION_3;
static uint8_t ui8_lights_configuration_temp = LIGHTS_CONFIGURATION_ON_STARTUP;

// system
static uint8_t ui8_riding_mode_parameter = 0;
volatile uint8_t ui8_system_state = NO_ERROR;
volatile uint8_t ui8_motor_enabled = 1;
static uint8_t ui8_assist_without_pedal_rotation_threshold = ASSISTANCE_WITHOUT_PEDAL_ROTATION_THRESHOLD;
volatile uint8_t ui8_lights_state = 0;
static uint8_t ui8_optional_ADC_function = OPTIONAL_ADC_FUNCTION;
volatile uint8_t ui8_optional_ADC_function_temp = OPTIONAL_ADC_FUNCTION;
static uint8_t ui8_motor_blocked_counter_threshold = MOTOR_BLOCKED_COUNTER_THRESHOLD * 4;
static uint8_t ui8_walk_assist_counter = 0;
static uint8_t ui8_walk_assist_level = 0;

// battery
volatile uint16_t ui16_battery_voltage_filtered_x10 = 0;
volatile uint16_t ui16_battery_voltage_calibrated_x10 = 0;
volatile uint16_t ui16_battery_voltage_soc_filtered_x10 = 0;															  
volatile uint16_t ui16_battery_power_filtered_x10 = 0;
volatile uint16_t ui16_actual_battery_capacity = (uint16_t)(((uint32_t) TARGET_MAX_BATTERY_CAPACITY * ACTUAL_BATTERY_CAPACITY_PERCENT) / 100);
volatile uint32_t ui32_wh_x10 = 0;
volatile uint32_t ui32_wh_sum_x10 = 0;
volatile uint32_t ui32_wh_x10_offset = 0;
volatile uint32_t ui32_wh_since_power_on_x10 = 0;
volatile uint16_t ui16_battery_SOC_percentage_x10 = 0;
volatile uint8_t ui8_battery_SOC_init_flag = 0;
volatile uint8_t ui8_battery_state_of_charge = 0;

// power control
static uint8_t ui8_duty_cycle_ramp_up_inverse_step = PWM_DUTY_CYCLE_RAMP_UP_INVERSE_STEP_DEFAULT;
static uint8_t ui8_duty_cycle_ramp_up_inverse_step_default = PWM_DUTY_CYCLE_RAMP_UP_INVERSE_STEP_DEFAULT;
static uint8_t ui8_duty_cycle_ramp_down_inverse_step = PWM_DUTY_CYCLE_RAMP_DOWN_INVERSE_STEP_DEFAULT;
static uint8_t ui8_duty_cycle_ramp_down_inverse_step_default = PWM_DUTY_CYCLE_RAMP_DOWN_INVERSE_STEP_DEFAULT;
static uint16_t ui16_battery_voltage_filtered_x1000 = 0;
static uint16_t ui16_battery_no_load_voltage_filtered_x10 = 0;
static uint8_t ui8_battery_current_filtered_x10 = 0;
static uint8_t ui8_adc_battery_current_max = ADC_10_BIT_BATTERY_CURRENT_MAX;
volatile uint8_t ui8_adc_battery_current_target = 0;
static uint8_t ui8_duty_cycle_target = 0;
static uint8_t ui8_target_battery_max_power_div25 = TARGET_MAX_BATTERY_POWER_DIV25;
volatile uint8_t ui8_adc_motor_phase_current_max = ADC_10_BIT_MOTOR_PHASE_CURRENT_MAX;

// brakes
static uint8_t ui8_brakes_engaged = 0;

// cadence sensor
uint16_t ui16_cadence_ticks_count_min_speed_adj = CADENCE_SENSOR_CALC_COUNTER_MIN;
static uint8_t ui8_pedal_cadence_RPM = 0;
uint8_t ui8_pedal_cadence_fast_stop = 0;
static uint8_t ui8_motor_deceleration = MOTOR_DECELERATION;

// torque sensor
static uint8_t ui8_pedal_torque_per_10_bit_ADC_step_x100 = PEDAL_TORQUE_PER_10_BIT_ADC_STEP_X100;
static uint8_t ui8_pedal_torque_per_10_bit_ADC_step_calc_x100 = PEDAL_TORQUE_PER_10_BIT_ADC_STEP_CALC_X100;
static uint16_t ui16_adc_pedal_torque_offset = PEDAL_TORQUE_ADC_OFFSET;
static uint16_t ui16_adc_pedal_torque_offset_init = PEDAL_TORQUE_ADC_OFFSET;
volatile uint16_t ui16_adc_pedal_torque_offset_cal = PEDAL_TORQUE_ADC_OFFSET;
static uint16_t ui16_adc_pedal_torque_offset_min = PEDAL_TORQUE_ADC_OFFSET - ADC_TORQUE_SENSOR_OFFSET_THRESHOLD;
static uint16_t ui16_adc_pedal_torque_offset_max = PEDAL_TORQUE_ADC_OFFSET + ADC_TORQUE_SENSOR_OFFSET_THRESHOLD;
static uint8_t ui8_adc_pedal_torque_offset_error = 0;
volatile uint16_t ui16_adc_coaster_brake_threshold = 0;
static uint16_t ui16_adc_pedal_torque = 0;
static uint16_t ui16_adc_pedal_torque_delta = 0;
static uint16_t ui16_adc_pedal_torque_delta_temp = 0;
static uint16_t ui16_adc_pedal_torque_delta_no_boost = 0;
static uint16_t ui16_pedal_torque_x100 = 0;
static uint16_t ui16_human_power_x10 = 0;
static uint16_t ui16_human_power_filtered_x10 = 0;
static uint8_t ui8_torque_sensor_calibrated = TORQUE_SENSOR_CALIBRATED;
static uint16_t ui16_pedal_weight_x100 = 0;
static uint16_t ui16_pedal_torque_step_temp = 0;
static uint8_t ui8_torque_sensor_calibration_flag = 0;
static uint8_t ui8_torque_sensor_calibration_started = 0;
volatile uint8_t ui8_riding_torque_mode = 0;

// wheel speed sensor
static uint16_t ui16_wheel_speed_x10 = 0;

// wheel speed display
static uint8_t ui8_display_ready_flag = 0;
static uint8_t ui8_startup_counter = 0;
static uint16_t ui16_oem_wheel_speed = 0;
static uint16_t ui16_oem_wheel_speed_max = 0;
static uint8_t ui8_oem_wheel_diameter = 0;
static uint32_t ui32_odometer_compensation_mm = ZERO_ODOMETER_COMPENSATION;

// throttle control
static uint8_t ui8_throttle_adc = 0;
static uint8_t ui8_street_mode_throttle_legal = STREET_MODE_THROTTLE_LEGAL;

// motor temperature control
static uint16_t ui16_adc_motor_temperature_filtered = 0;
static uint16_t ui16_motor_temperature_filtered_x10 = 0;
static uint8_t ui8_motor_temperature_max_value_to_limit = MOTOR_TEMPERATURE_MAX_VALUE_LIMIT;
static uint8_t ui8_motor_temperature_min_value_to_limit = MOTOR_TEMPERATURE_MIN_VALUE_LIMIT;

// eMTB assist
#define eMTB_POWER_FUNCTION_ARRAY_SIZE      241

static const uint8_t ui8_eMTB_power_function_160[eMTB_POWER_FUNCTION_ARRAY_SIZE] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5,
        5, 5, 5, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 9, 9, 9, 9, 9, 10, 10, 10, 10, 10, 11, 11, 11, 11, 12,
        12, 12, 12, 12, 13, 13, 13, 13, 14, 14, 14, 14, 15, 15, 15, 15, 16, 16, 16, 16, 17, 17, 17, 17, 18, 18, 18, 18,
        19, 19, 19, 20, 20, 20, 20, 21, 21, 21, 22, 22, 22, 22, 23, 23, 23, 24, 24, 24, 24, 25, 25, 25, 26, 26, 26, 27,
        27, 27, 27, 28, 28, 28, 29, 29, 29, 30, 30, 30, 31, 31, 31, 32, 32, 32, 33, 33, 33, 34, 34, 34, 35, 35, 35, 36,
        36, 36, 37, 37, 37, 38, 38, 38, 39, 39, 40, 40, 40, 41, 41, 41, 42, 42, 42, 43, 43, 44, 44, 44, 45, 45, 45, 46,
        46, 47, 47, 47, 48, 48, 48, 49, 49, 50, 50, 50, 51, 51, 52, 52, 52, 53, 53, 54, 54, 54, 55, 55, 56, 56, 56, 57,
        57, 58, 58, 58, 59, 59, 60, 60, 61, 61, 61, 62, 62, 63, 63, 63, 64, 64 };
static const uint8_t ui8_eMTB_power_function_165[eMTB_POWER_FUNCTION_ARRAY_SIZE] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 6, 6, 6,
        6, 6, 7, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9, 10, 10, 10, 10, 11, 11, 11, 11, 12, 12, 12, 12, 13, 13, 13, 14, 14,
        14, 14, 15, 15, 15, 16, 16, 16, 16, 17, 17, 17, 18, 18, 18, 19, 19, 19, 20, 20, 20, 21, 21, 21, 22, 22, 22, 23,
        23, 23, 24, 24, 24, 25, 25, 25, 26, 26, 27, 27, 27, 28, 28, 28, 29, 29, 30, 30, 30, 31, 31, 32, 32, 32, 33, 33,
        34, 34, 34, 35, 35, 36, 36, 36, 37, 37, 38, 38, 39, 39, 39, 40, 40, 41, 41, 42, 42, 42, 43, 43, 44, 44, 45, 45,
        46, 46, 47, 47, 47, 48, 48, 49, 49, 50, 50, 51, 51, 52, 52, 53, 53, 54, 54, 55, 55, 56, 56, 57, 57, 58, 58, 59,
        59, 60, 60, 61, 61, 62, 62, 63, 63, 64, 64, 65, 65, 66, 66, 67, 67, 68, 68, 69, 69, 70, 71, 71, 72, 72, 73, 73,
        74, 74, 75, 75, 76, 77, 77, 78, 78, 79, 79, 80, 81, 81, 82, 82, 83, 83, 84, 85 };
static const uint8_t ui8_eMTB_power_function_170[eMTB_POWER_FUNCTION_ARRAY_SIZE] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 5, 5, 5, 5, 6, 6, 6, 6, 6, 7, 7, 7,
        7, 8, 8, 8, 9, 9, 9, 9, 10, 10, 10, 11, 11, 11, 11, 12, 12, 12, 13, 13, 13, 14, 14, 14, 15, 15, 15, 16, 16, 16,
        17, 17, 18, 18, 18, 19, 19, 19, 20, 20, 21, 21, 21, 22, 22, 23, 23, 23, 24, 24, 25, 25, 26, 26, 26, 27, 27, 28,
        28, 29, 29, 30, 30, 30, 31, 31, 32, 32, 33, 33, 34, 34, 35, 35, 36, 36, 37, 37, 38, 38, 39, 39, 40, 40, 41, 41,
        42, 42, 43, 43, 44, 45, 45, 46, 46, 47, 47, 48, 48, 49, 49, 50, 51, 51, 52, 52, 53, 53, 54, 55, 55, 56, 56, 57,
        58, 58, 59, 59, 60, 61, 61, 62, 63, 63, 64, 64, 65, 66, 66, 67, 68, 68, 69, 70, 70, 71, 71, 72, 73, 73, 74, 75,
        75, 76, 77, 77, 78, 79, 80, 80, 81, 82, 82, 83, 84, 84, 85, 86, 87, 87, 88, 89, 89, 90, 91, 92, 92, 93, 94, 94,
        95, 96, 97, 97, 98, 99, 100, 100, 101, 102, 103, 103, 104, 105, 106, 107, 107, 108, 109, 110, 110, 111 };
static const uint8_t ui8_eMTB_power_function_175[eMTB_POWER_FUNCTION_ARRAY_SIZE] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1,
        1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 6, 6, 6, 6, 7, 7, 7, 8, 8, 8, 8, 9,
        9, 9, 10, 10, 10, 11, 11, 11, 12, 12, 13, 13, 13, 14, 14, 14, 15, 15, 16, 16, 17, 17, 17, 18, 18, 19, 19, 20,
        20, 20, 21, 21, 22, 22, 23, 23, 24, 24, 25, 25, 26, 26, 27, 27, 28, 28, 29, 29, 30, 31, 31, 32, 32, 33, 33, 34,
        34, 35, 36, 36, 37, 37, 38, 39, 39, 40, 40, 41, 42, 42, 43, 44, 44, 45, 45, 46, 47, 47, 48, 49, 49, 50, 51, 51,
        52, 53, 53, 54, 55, 56, 56, 57, 58, 58, 59, 60, 61, 61, 62, 63, 64, 64, 65, 66, 67, 67, 68, 69, 70, 70, 71, 72,
        73, 74, 74, 75, 76, 77, 78, 78, 79, 80, 81, 82, 83, 83, 84, 85, 86, 87, 88, 88, 89, 90, 91, 92, 93, 94, 95, 95,
        96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117,
        118, 119, 120, 121, 122, 123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139,
        140, 141, 142, 143, 144, 145, 146 };
static const uint8_t ui8_eMTB_power_function_180[eMTB_POWER_FUNCTION_ARRAY_SIZE] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1,
        1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 6, 6, 6, 7, 7, 7, 8, 8, 8, 9, 9, 9, 10, 10,
        11, 11, 11, 12, 12, 13, 13, 14, 14, 14, 15, 15, 16, 16, 17, 17, 18, 18, 19, 19, 20, 20, 21, 21, 22, 23, 23, 24,
        24, 25, 25, 26, 27, 27, 28, 28, 29, 30, 30, 31, 32, 32, 33, 34, 34, 35, 36, 36, 37, 38, 38, 39, 40, 41, 41, 42,
        43, 43, 44, 45, 46, 46, 47, 48, 49, 50, 50, 51, 52, 53, 54, 54, 55, 56, 57, 58, 59, 59, 60, 61, 62, 63, 64, 65,
        66, 67, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92,
        93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 105, 106, 107, 108, 109, 110, 111, 112, 114, 115, 116, 117, 118,
        119, 120, 122, 123, 124, 125, 126, 128, 129, 130, 131, 132, 134, 135, 136, 137, 139, 140, 141, 142, 144, 145,
        146, 147, 149, 150, 151, 153, 154, 155, 157, 158, 159, 161, 162, 163, 165, 166, 167, 169, 170, 171, 173, 174,
        176, 177, 178, 180, 181, 182, 184, 185, 187, 188, 190, 191, 192 };
static const uint8_t ui8_eMTB_power_function_185[eMTB_POWER_FUNCTION_ARRAY_SIZE] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1,
        1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 6, 6, 6, 7, 7, 8, 8, 8, 9, 9, 10, 10, 11, 11, 11, 12,
        12, 13, 13, 14, 14, 15, 15, 16, 17, 17, 18, 18, 19, 19, 20, 21, 21, 22, 23, 23, 24, 25, 25, 26, 27, 27, 28, 29,
        29, 30, 31, 32, 32, 33, 34, 35, 36, 36, 37, 38, 39, 40, 40, 41, 42, 43, 44, 45, 46, 46, 47, 48, 49, 50, 51, 52,
        53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 74, 75, 76, 77, 78, 79, 80, 81,
        83, 84, 85, 86, 87, 89, 90, 91, 92, 93, 95, 96, 97, 98, 100, 101, 102, 104, 105, 106, 107, 109, 110, 111, 113,
        114, 115, 117, 118, 120, 121, 122, 124, 125, 127, 128, 129, 131, 132, 134, 135, 137, 138, 140, 141, 143, 144,
        146, 147, 149, 150, 152, 153, 155, 156, 158, 160, 161, 163, 164, 166, 168, 169, 171, 172, 174, 176, 177, 179,
        181, 182, 184, 186, 187, 189, 191, 193, 194, 196, 198, 199, 201, 203, 205, 207, 208, 210, 212, 214, 216, 217,
        219, 221, 223, 225, 227, 228, 230, 232, 234, 236, 238, 240, 240, 240, 240, 240, 240, 240, 240 };
static const uint8_t ui8_eMTB_power_function_190[eMTB_POWER_FUNCTION_ARRAY_SIZE] = { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1,
        1, 1, 2, 2, 2, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6, 6, 7, 7, 8, 8, 9, 9, 10, 10, 11, 11, 12, 12, 13, 13, 14,
        14, 15, 16, 16, 17, 18, 18, 19, 20, 20, 21, 22, 22, 23, 24, 25, 25, 26, 27, 28, 29, 29, 30, 31, 32, 33, 34, 35,
        36, 37, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 51, 52, 53, 54, 55, 56, 57, 58, 60, 61, 62, 63, 64,
        66, 67, 68, 69, 70, 72, 73, 74, 76, 77, 78, 80, 81, 82, 84, 85, 86, 88, 89, 91, 92, 94, 95, 96, 98, 99, 101,
        102, 104, 105, 107, 108, 110, 112, 113, 115, 116, 118, 120, 121, 123, 124, 126, 128, 130, 131, 133, 135, 136,
        138, 140, 142, 143, 145, 147, 149, 150, 152, 154, 156, 158, 160, 162, 163, 165, 167, 169, 171, 173, 175, 177,
        179, 181, 183, 185, 187, 189, 191, 193, 195, 197, 199, 201, 203, 205, 207, 209, 211, 214, 216, 218, 220, 222,
        224, 227, 229, 231, 233, 235, 238, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240 };
static const uint8_t ui8_eMTB_power_function_195[eMTB_POWER_FUNCTION_ARRAY_SIZE] = { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1,
        1, 1, 2, 2, 2, 3, 3, 3, 3, 4, 4, 5, 5, 5, 6, 6, 7, 7, 8, 8, 9, 9, 10, 10, 11, 11, 12, 13, 13, 14, 15, 15, 16,
        17, 17, 18, 19, 20, 21, 21, 22, 23, 24, 25, 26, 27, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 39, 40, 41, 42,
        43, 44, 45, 47, 48, 49, 50, 51, 53, 54, 55, 57, 58, 59, 61, 62, 63, 65, 66, 68, 69, 70, 72, 73, 75, 76, 78, 79,
        81, 83, 84, 86, 87, 89, 91, 92, 94, 96, 97, 99, 101, 103, 104, 106, 108, 110, 112, 113, 115, 117, 119, 121, 123,
        125, 127, 129, 131, 132, 134, 136, 139, 141, 143, 145, 147, 149, 151, 153, 155, 157, 160, 162, 164, 166, 168,
        171, 173, 175, 177, 180, 182, 184, 187, 189, 191, 194, 196, 199, 201, 203, 206, 208, 211, 213, 216, 218, 221,
        224, 226, 229, 231, 234, 237, 239, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240 };
static const uint8_t ui8_eMTB_power_function_200[eMTB_POWER_FUNCTION_ARRAY_SIZE] = { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1,
        1, 2, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 10, 10, 11, 12, 12, 13, 14, 14, 15, 16, 17, 18, 18, 19,
        20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 34, 35, 36, 37, 38, 40, 41, 42, 44, 45, 46, 48, 49, 50, 52,
        53, 55, 56, 58, 59, 61, 62, 64, 66, 67, 69, 71, 72, 74, 76, 77, 79, 81, 83, 85, 86, 88, 90, 92, 94, 96, 98, 100,
        102, 104, 106, 108, 110, 112, 114, 117, 119, 121, 123, 125, 128, 130, 132, 135, 137, 139, 142, 144, 146, 149,
        151, 154, 156, 159, 161, 164, 166, 169, 172, 174, 177, 180, 182, 185, 188, 190, 193, 196, 199, 202, 204, 207,
        210, 213, 216, 219, 222, 225, 228, 231, 234, 237, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240 };
static const uint8_t ui8_eMTB_power_function_205[eMTB_POWER_FUNCTION_ARRAY_SIZE] = { 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1,
        2, 2, 2, 3, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 9, 9, 10, 11, 11, 12, 13, 14, 15, 16, 16, 17, 18, 19, 20, 21, 22,
        23, 24, 26, 27, 28, 29, 30, 32, 33, 34, 36, 37, 38, 40, 41, 43, 44, 46, 47, 49, 50, 52, 54, 55, 57, 59, 61, 62,
        64, 66, 68, 70, 72, 74, 76, 78, 80, 82, 84, 86, 88, 90, 92, 95, 97, 99, 101, 104, 106, 108, 111, 113, 116, 118,
        121, 123, 126, 128, 131, 134, 136, 139, 142, 145, 147, 150, 153, 156, 159, 162, 165, 168, 171, 174, 177, 180,
        183, 186, 189, 192, 196, 199, 202, 205, 209, 212, 216, 219, 222, 226, 229, 233, 236, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240 };
static const uint8_t ui8_eMTB_power_function_210[eMTB_POWER_FUNCTION_ARRAY_SIZE] = { 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2,
        2, 2, 3, 3, 3, 4, 4, 5, 5, 6, 7, 7, 8, 9, 9, 10, 11, 12, 13, 14, 14, 15, 16, 17, 19, 20, 21, 22, 23, 24, 26, 27,
        28, 30, 31, 32, 34, 35, 37, 39, 40, 42, 43, 45, 47, 49, 50, 52, 54, 56, 58, 60, 62, 64, 66, 68, 71, 73, 75, 77,
        80, 82, 84, 87, 89, 92, 94, 97, 99, 102, 104, 107, 110, 113, 115, 118, 121, 124, 127, 130, 133, 136, 139, 142,
        145, 149, 152, 155, 158, 162, 165, 169, 172, 176, 179, 183, 186, 190, 194, 197, 201, 205, 209, 213, 216, 220,
        224, 228, 232, 237, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240 };
static const uint8_t ui8_eMTB_power_function_215[eMTB_POWER_FUNCTION_ARRAY_SIZE] = { 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2,
        2, 2, 3, 3, 4, 4, 5, 6, 6, 7, 8, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 20, 21, 22, 24, 25, 26, 28, 29, 31,
        33, 34, 36, 38, 39, 41, 43, 45, 47, 49, 51, 53, 55, 57, 60, 62, 64, 67, 69, 71, 74, 76, 79, 82, 84, 87, 90, 93,
        96, 98, 101, 104, 107, 111, 114, 117, 120, 123, 127, 130, 134, 137, 141, 144, 148, 152, 155, 159, 163, 167, 171,
        175, 179, 183, 187, 191, 195, 200, 204, 208, 213, 217, 222, 226, 231, 235, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240 };
static const uint8_t ui8_eMTB_power_function_220[eMTB_POWER_FUNCTION_ARRAY_SIZE] = { 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 2,
        2, 3, 3, 4, 4, 5, 6, 7, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 18, 19, 20, 22, 23, 25, 27, 28, 30, 32, 33, 35, 37,
        39, 41, 43, 46, 48, 50, 52, 55, 57, 60, 62, 65, 67, 70, 73, 76, 79, 82, 85, 88, 91, 94, 97, 101, 104, 108, 111,
        115, 118, 122, 126, 130, 133, 137, 141, 145, 150, 154, 158, 162, 167, 171, 176, 180, 185, 190, 194, 199, 204,
        209, 214, 219, 224, 230, 235, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240 };
static const uint8_t ui8_eMTB_power_function_225[eMTB_POWER_FUNCTION_ARRAY_SIZE] = { 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 2,
        3, 3, 4, 4, 5, 6, 7, 8, 8, 9, 10, 12, 13, 14, 15, 17, 18, 20, 21, 23, 24, 26, 28, 30, 32, 34, 36, 38, 40, 43,
        45, 47, 50, 52, 55, 58, 61, 64, 66, 70, 73, 76, 79, 82, 86, 89, 93, 96, 100, 104, 108, 112, 116, 120, 124, 128,
        133, 137, 142, 146, 151, 156, 161, 166, 171, 176, 181, 186, 191, 197, 202, 208, 214, 219, 225, 231, 237, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240 };
static const uint8_t ui8_eMTB_power_function_230[eMTB_POWER_FUNCTION_ARRAY_SIZE] = { 0, 0, 0, 0, 0, 0, 1, 1, 1, 2, 2, 2,
        3, 4, 4, 5, 6, 7, 8, 9, 10, 11, 12, 14, 15, 16, 18, 20, 21, 23, 25, 27, 29, 31, 33, 36, 38, 40, 43, 46, 48, 51,
        54, 57, 60, 63, 67, 70, 74, 77, 81, 85, 88, 92, 96, 101, 105, 109, 114, 118, 123, 128, 133, 138, 143, 148, 153,
        158, 164, 170, 175, 181, 187, 193, 199, 205, 212, 218, 225, 231, 238, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240 };
static const uint8_t ui8_eMTB_power_function_235[eMTB_POWER_FUNCTION_ARRAY_SIZE] = { 0, 0, 0, 0, 0, 0, 1, 1, 1, 2, 2, 3,
        3, 4, 5, 6, 7, 8, 9, 10, 11, 13, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 37, 40, 43, 45, 48, 52, 55, 58, 62,
        65, 69, 73, 77, 81, 85, 89, 94, 98, 103, 108, 113, 118, 123, 128, 134, 139, 145, 151, 157, 163, 169, 176, 182,
        189, 196, 202, 210, 217, 224, 232, 239, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240 };
static const uint8_t ui8_eMTB_power_function_240[eMTB_POWER_FUNCTION_ARRAY_SIZE] = { 0, 0, 0, 0, 0, 0, 1, 1, 1, 2, 3, 3,
        4, 5, 6, 7, 8, 9, 10, 12, 13, 15, 17, 19, 21, 23, 25, 27, 30, 32, 35, 38, 41, 44, 47, 51, 54, 58, 62, 66, 70,
        74, 79, 83, 88, 93, 98, 103, 108, 114, 120, 125, 131, 137, 144, 150, 157, 164, 171, 178, 185, 193, 200, 208,
        216, 224, 233, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240 };
static const uint8_t ui8_eMTB_power_function_245[eMTB_POWER_FUNCTION_ARRAY_SIZE] = { 0, 0, 0, 0, 0, 1, 1, 1, 2, 2, 3, 4,
        4, 5, 6, 8, 9, 10, 12, 14, 15, 17, 19, 22, 24, 27, 29, 32, 35, 38, 42, 45, 49, 53, 57, 61, 65, 70, 74, 79, 84,
        89, 95, 100, 106, 112, 119, 125, 132, 138, 145, 153, 160, 168, 176, 184, 192, 200, 209, 218, 227, 237, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240 };
static const uint8_t ui8_eMTB_power_function_250[eMTB_POWER_FUNCTION_ARRAY_SIZE] = { 0, 0, 0, 0, 0, 1, 1, 1, 2, 2, 3, 4,
        5, 6, 7, 9, 10, 12, 14, 16, 18, 20, 23, 25, 28, 31, 34, 38, 41, 45, 49, 54, 58, 63, 67, 72, 78, 83, 89, 95, 101,
        108, 114, 121, 128, 136, 144, 151, 160, 168, 177, 186, 195, 204, 214, 224, 235, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240 };
static const uint8_t ui8_eMTB_power_function_255[eMTB_POWER_FUNCTION_ARRAY_SIZE] = { 0, 0, 0, 0, 0, 1, 1, 1, 2, 3, 4, 5,
        6, 7, 8, 10, 12, 14, 16, 18, 21, 24, 26, 30, 33, 37, 41, 45, 49, 54, 58, 64, 69, 75, 80, 87, 93, 100, 107, 114,
        122, 130, 138, 146, 155, 164, 174, 184, 194, 204, 215, 226, 238, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240,
        240, 240, 240 };

// walk assist
static uint8_t ui8_walk_assist_speed_target_x10 = 0;
static uint8_t ui8_walk_assist_duty_cycle_counter = 0;
static uint8_t ui8_walk_assist_duty_cycle_target = WALK_ASSIST_DUTY_CYCLE_MIN;
static uint8_t ui8_walk_assist_duty_cycle_max = WALK_ASSIST_DUTY_CYCLE_MIN;
static uint8_t ui8_walk_assist_adj_delay = WALK_ASSIST_ADJ_DELAY_MIN;
static uint16_t ui16_walk_assist_wheel_speed_counter = 0;
static uint16_t ui16_walk_assist_erps_target = 0;
static uint16_t ui16_walk_assist_erps_min = 0;
static uint16_t ui16_walk_assist_erps_max = 0;
static uint8_t ui8_walk_assist_speed_flag = 0;

// cruise
static uint8_t ui8_cruise_threshold_speed_x10_offroad = CRUISE_THRESHOLD_SPEED_X10;
static uint8_t ui8_cruise_threshold_speed_x10_street = CRUISE_THRESHOLD_SPEED_X10;
static uint8_t ui8_cruise_PID_initialize = 1;
volatile uint8_t ui8_cruise_counter = 0;
volatile uint8_t ui8_cruise_button_flag = 0;

// startup boost
static uint8_t ui8_startup_boost_at_zero = STARTUP_BOOST_AT_ZERO;
static uint8_t ui8_startup_boost_flag = 0;
static uint8_t ui8_startup_boost_enabled_temp = STARTUP_BOOST_ON_STARTUP;
static uint16_t ui16_startup_boost_factor_array[120];

// startup assist
static uint8_t ui8_startup_assist = 0;
static uint8_t ui8_startup_assist_counter = 0;
static uint8_t ui8_startup_assist_adc_battery_current_target = 0;

// UART
volatile uint8_t ui8_received_package_flag = 0;
volatile uint8_t ui8_rx_buffer[UART_RX_BUFFER_LEN];
volatile uint8_t ui8_rx_counter = 0;
volatile uint8_t ui8_tx_buffer[UART_TX_BUFFER_LEN];
volatile uint8_t ui8_byte_received;
volatile uint8_t ui8_state_machine = 0;

// uart send
volatile uint8_t ui8_working_status = 0;
volatile uint8_t ui8_display_fault_code = 0;

// array for oem display
static uint16_t ui16_data_value_array[DATA_VALUE_ARRAY_DIM];
static uint8_t ui8_data_index_array[DATA_INDEX_ARRAY_DIM] = {DISPLAY_DATA_1,DISPLAY_DATA_2,DISPLAY_DATA_3,DISPLAY_DATA_4,DISPLAY_DATA_5,DISPLAY_DATA_6};
static uint8_t ui8_delay_display_array[DATA_INDEX_ARRAY_DIM] = {DELAY_DISPLAY_DATA_1,DELAY_DISPLAY_DATA_2,DELAY_DISPLAY_DATA_3,DELAY_DISPLAY_DATA_4,DELAY_DISPLAY_DATA_5,DELAY_DISPLAY_DATA_6};
		
// array for riding parameters
static uint8_t  ui8_riding_mode_parameter_array[8][5] = {
	{POWER_ASSIST_LEVEL_OFF, POWER_ASSIST_LEVEL_ECO, POWER_ASSIST_LEVEL_TOUR, POWER_ASSIST_LEVEL_SPORT, POWER_ASSIST_LEVEL_TURBO},
	{TORQUE_ASSIST_LEVEL_0, TORQUE_ASSIST_LEVEL_1, TORQUE_ASSIST_LEVEL_2, TORQUE_ASSIST_LEVEL_3, TORQUE_ASSIST_LEVEL_4},
	{CADENCE_ASSIST_LEVEL_0, CADENCE_ASSIST_LEVEL_1, CADENCE_ASSIST_LEVEL_2, CADENCE_ASSIST_LEVEL_3, CADENCE_ASSIST_LEVEL_4},
	{EMTB_ASSIST_LEVEL_0, EMTB_ASSIST_LEVEL_1, EMTB_ASSIST_LEVEL_2, EMTB_ASSIST_LEVEL_3, EMTB_ASSIST_LEVEL_4},
	{POWER_ASSIST_LEVEL_OFF, POWER_ASSIST_LEVEL_ECO, POWER_ASSIST_LEVEL_TOUR, POWER_ASSIST_LEVEL_SPORT, POWER_ASSIST_LEVEL_TURBO},
	{CRUISE_TARGET_SPEED_LEVEL_0, CRUISE_TARGET_SPEED_LEVEL_1, CRUISE_TARGET_SPEED_LEVEL_2, CRUISE_TARGET_SPEED_LEVEL_3, CRUISE_TARGET_SPEED_LEVEL_4},
	{WALK_ASSIST_LEVEL_0, WALK_ASSIST_LEVEL_1, WALK_ASSIST_LEVEL_2, WALK_ASSIST_LEVEL_3, WALK_ASSIST_LEVEL_4},
	{0, 0, 0, 0, 0}
	};
		
// communications functions
static void uart_receive_package(void);
static void uart_send_package(void);

// system functions
static void get_battery_voltage_filtered(void);
static void get_battery_current_filtered(void);
static void get_pedal_torque(void);
static void calc_wheel_speed(void);
static void calc_cadence(void);

static void ebike_control_lights(void);
static void ebike_control_motor(void);
static void check_system(void);
static void check_brakes(void);

static void apply_power_assist(void);
static void apply_torque_assist(void);
static void apply_cadence_assist(void);
static void apply_emtb_assist(void);
static void apply_hybrid_assist(void);
static void apply_cruise(void);
static void apply_walk_assist(void);
static void apply_throttle(void);
static void apply_temperature_limiting(void);
static void apply_speed_limit(void);

// functions for oem display
static void calc_oem_wheel_speed(void);
static void apply_torque_sensor_calibration(void);

// battery soc percentage x10 calculation
static void check_battery_soc(void);
uint16_t read_battery_soc(void);
uint16_t calc_battery_soc_x10(uint16_t ui16_battery_soc_offset_x10, uint16_t ui16_battery_soc_step_x10, uint16_t ui16_cell_volts_max_x100, uint16_t ui16_cell_volts_min_x100);


void ebike_app_init(void)
{
	uint8_t ui8_i;
	
	// set low voltage cutoff (8 bit)  300>86
	ui8_adc_battery_voltage_cut_off = ((uint32_t) m_configuration_variables.ui16_battery_low_voltage_cut_off_x10 * 25) / BATTERY_VOLTAGE_PER_10_BIT_ADC_STEP_X1000;
	
	// check if assist without pedal rotation threshold is valid (safety)
	if (ui8_assist_without_pedal_rotation_threshold > 100)
		ui8_assist_without_pedal_rotation_threshold = 100;
	
	// check the cruise speed threshold x10 in street mode
	if(ui8_cruise_threshold_speed_x10_offroad < CRUISE_THRESHOLD_SPEED_X10_DEFAULT)
		ui8_cruise_threshold_speed_x10_street = CRUISE_THRESHOLD_SPEED_X10_DEFAULT;
	
	// set duty cycle ramp up inverse step default
	ui8_duty_cycle_ramp_up_inverse_step_default = map_ui8((uint8_t) MOTOR_ACCELERATION,
				(uint8_t) 0,
				(uint8_t) 100,
				(uint8_t) PWM_DUTY_CYCLE_RAMP_UP_INVERSE_STEP_DEFAULT,
				(uint8_t) PWM_DUTY_CYCLE_RAMP_UP_INVERSE_STEP_MIN);
	
	// set duty cycle ramp down inverse step default
	ui8_duty_cycle_ramp_down_inverse_step_default = map_ui8((uint8_t) MOTOR_DECELERATION,
				(uint8_t) 0,
                (uint8_t) 100,
                (uint8_t) PWM_DUTY_CYCLE_RAMP_DOWN_INVERSE_STEP_DEFAULT,
                (uint8_t) PWM_DUTY_CYCLE_RAMP_DOWN_INVERSE_STEP_MIN);
	
	// set pedal cadence fast stop
	if(ui8_motor_deceleration == 100)
		ui8_pedal_cadence_fast_stop = 1;
	else
		ui8_pedal_cadence_fast_stop = 0;
	
	// set pedal torque per 10_bit DC_step x100 advanced
	if((ui8_torque_sensor_calibrated)&&(m_configuration_variables.ui8_torque_sensor_adv_enabled)) {
		ui8_pedal_torque_per_10_bit_ADC_step_x100 = PEDAL_TORQUE_PER_10_BIT_ADC_STEP_ADV_X100;
	}
	
	// parameters status on startup
	// set parameters on startup
	ui8_display_function_status[0][OFF] = m_configuration_variables.ui8_set_parameter_enabled;
	// auto display data on startup
	ui8_display_function_status[1][OFF] = m_configuration_variables.ui8_auto_display_data_enabled;
	// street mode on startup
	ui8_display_function_status[0][ECO] = m_configuration_variables.ui8_street_mode_enabled;
	// startup boost on startup
	ui8_display_function_status[1][ECO] = m_configuration_variables.ui8_startup_boost_enabled;
	// torque sensor adv on startup
	ui8_display_function_status[2][ECO] = m_configuration_variables.ui8_torque_sensor_adv_enabled;
	// assist without pedal rotation on startup
	ui8_display_function_status[1][TURBO] = m_configuration_variables.ui8_assist_without_pedal_rotation_enabled;
	// system error enabled on startup
	ui8_display_function_status[2][TURBO] = m_configuration_variables.ui8_assist_with_error_enabled;
	// riding mode on startup
	ui8_display_riding_mode = m_configuration_variables.ui8_riding_mode;
	// lights configuration on startup
	ui8_display_lights_configuration = m_configuration_variables.ui8_lights_configuration;
	
	// percentage remaining battery capacity x10 at power on
	ui16_battery_SOC_percentage_x10 = ((uint16_t) m_configuration_variables.ui8_battery_SOC_percentage_8b) << 2;
		 
	// battery SOC checked at power on
	if(ui16_battery_SOC_percentage_x10)
	{
		// calculate watt-hours x10 at power on
		ui32_wh_x10_offset = ((uint32_t)(1000 - ui16_battery_SOC_percentage_x10) * ui16_actual_battery_capacity) / 100;
		
		ui8_battery_SOC_init_flag = 1;
	}

	// make startup boost array
	ui16_startup_boost_factor_array[0] = STARTUP_BOOST_TORQUE_FACTOR;
		
	for (ui8_i = 1; ui8_i < 120; ui8_i++)
	{
		uint16_t ui16_temp = (ui16_startup_boost_factor_array[ui8_i - 1] * STARTUP_BOOST_CADENCE_STEP) >> 8;
		ui16_startup_boost_factor_array[ui8_i] = ui16_startup_boost_factor_array[ui8_i - 1] - ui16_temp;
	}
	
	// enable data displayed on startup
	#if DATA_DISPLAY_ON_STARTUP
	ui8_display_data_enabled = 1;
	#endif
}


void ebike_app_controller(void)
{
    calc_wheel_speed();	// calculate the wheel speed
    calc_cadence();		// calculate the cadence and set limits from wheel speed

    get_battery_voltage_filtered(); // get filtered voltage from FOC calculations
    get_battery_current_filtered(); // get filtered current from FOC calculations
    get_pedal_torque();				// get pedal torque

    check_system();					// check if there are any errors for motor control
    check_brakes();					// check if brakes are enabled for motor control

    // send/receive data every 4 cycles (25ms * 4)
	static uint8_t ui8_counter;
	
	switch (ui8_counter++ & 0x03) {
		case 0: 
			uart_receive_package();
			break;
		case 1:
			ebike_control_lights();
			calc_oem_wheel_speed();
			break;
		case 2:
			uart_send_package();
			break;
		case 3:
			check_battery_soc();
			break;
	}
	
	// use received data and sensor input to control motor
    ebike_control_motor();

    /*------------------------------------------------------------------------

     NOTE: regarding function call order

     Do not change order of functions if not absolutely sure it will
     not cause any undesirable consequences.

     ------------------------------------------------------------------------*/
}


static void ebike_control_motor(void)
{
	// reset ebike loop check counter
	ui16_ebike_check_counter = 0;
	
	// assist level = OFF if the motor goes alone and with current or duty cycle target = 0 (safety)
	if((ui16_motor_speed_erps)&&(!ui8_adc_battery_current_target || !ui8_duty_cycle_target))
		ui8_assist_level = OFF;
	// set assist level in controller
	ui8_g_assist_level = ui8_assist_level;
	
    // reset control variables (safety)
    ui8_duty_cycle_ramp_up_inverse_step = PWM_DUTY_CYCLE_RAMP_UP_INVERSE_STEP_DEFAULT;
    ui8_duty_cycle_ramp_down_inverse_step = PWM_DUTY_CYCLE_RAMP_DOWN_INVERSE_STEP_DEFAULT;
    ui8_adc_battery_current_target = 0;
    ui8_duty_cycle_target = 0;

    // reset initialization of Cruise PID controller
    if (m_configuration_variables.ui8_riding_mode != CRUISE_MODE) {
		ui8_cruise_PID_initialize = 1;
	}

    // select riding mode
    switch (m_configuration_variables.ui8_riding_mode) {
		case POWER_ASSIST_MODE: apply_power_assist(); break;
		case TORQUE_ASSIST_MODE: apply_torque_assist(); break;
		case CADENCE_ASSIST_MODE: apply_cadence_assist(); break;
		case eMTB_ASSIST_MODE: apply_emtb_assist(); break;
		case HYBRID_ASSIST_MODE: apply_hybrid_assist(); break;
		case CRUISE_MODE: apply_cruise(); break;
		case WALK_ASSIST_MODE: apply_walk_assist(); break;
		case TORQUE_SENSOR_CALIBRATION_MODE: apply_torque_sensor_calibration(); break;
    }

    // select optional ADC function
    switch (ui8_optional_ADC_function) {
		case THROTTLE_CONTROL: apply_throttle(); break;
		case TEMPERATURE_CONTROL: apply_temperature_limiting(); break;
    }

    // speed limit
    apply_speed_limit();
	
    // check if to enable the motor
    if ((!ui8_motor_enabled) &&
		(ui16_motor_speed_erps == 0) && // only enable motor if stopped, else something bad can happen due to high currents/regen or similar
        (ui8_adc_battery_current_target) &&
		(!ui8_brakes_engaged)) {
			ui8_motor_enabled = 1;
			ui8_g_duty_cycle = 0;
			ui8_fw_angle = 0;
			motor_enable_pwm();
    }

    // check if to disable the motor
    if ((ui8_motor_enabled) &&
		(ui16_motor_speed_erps == 0) &&
		(!ui8_adc_battery_current_target) &&
		(!ui8_g_duty_cycle)) {
			ui8_motor_enabled = 0;
			motor_disable_pwm();
    }

    // reset control parameters if... (safety)
    if((ui8_brakes_engaged) || (!ui8_assist_level) ||
	  (ui8_system_state == ERROR_MOTOR_BLOCKED) ||
	  (ui8_system_state == ERROR_MOTOR_CHECK) ||
	  (!ui8_motor_enabled) ||
	  (ui8_riding_mode_parameter == 0) ||
	  ((ui8_system_state != NO_ERROR)&&(!m_configuration_variables.ui8_assist_with_error_enabled))) {
        ui8_controller_duty_cycle_ramp_up_inverse_step = PWM_DUTY_CYCLE_RAMP_UP_INVERSE_STEP_DEFAULT;
        ui8_controller_duty_cycle_ramp_down_inverse_step = PWM_DUTY_CYCLE_RAMP_DOWN_INVERSE_STEP_MIN;
        ui8_controller_adc_battery_current_target = 0;
        ui8_controller_duty_cycle_target = 0;
    } else {
        // limit max current if higher than configured hardware limit (safety)
        if (ui8_adc_battery_current_max > ADC_10_BIT_BATTERY_CURRENT_MAX) {
            ui8_adc_battery_current_max = ADC_10_BIT_BATTERY_CURRENT_MAX;
        }

        // limit target current if higher than max value (safety)
        if (ui8_adc_battery_current_target > ui8_adc_battery_current_max) {
            ui8_adc_battery_current_target = ui8_adc_battery_current_max;
        }

        // limit target duty cycle if higher than max value
        if (ui8_duty_cycle_target >= PWM_DUTY_CYCLE_MAX) {
            ui8_duty_cycle_target = PWM_DUTY_CYCLE_MAX;
        }
/*		
		if((ui16_motor_speed_erps < 50)&&(m_configuration_variables.ui8_riding_mode != CADENCE_ASSIST_MODE)) {
			ui8_duty_cycle_ramp_up_inverse_step = map_ui8((uint8_t)(ui16_motor_speed_erps),
				(uint8_t)0,
				(uint8_t)50,
				(uint8_t)PWM_DUTY_CYCLE_RAMP_UP_INVERSE_STEP_STARTUP,
				(uint8_t)ui8_duty_cycle_ramp_up_inverse_step_default);
			
			ui8_duty_cycle_ramp_down_inverse_step = PWM_DUTY_CYCLE_RAMP_DOWN_INVERSE_STEP_MIN;
		}
*/		
        // limit target duty cycle ramp up inverse step if lower than min value (safety)
        if (ui8_duty_cycle_ramp_up_inverse_step < PWM_DUTY_CYCLE_RAMP_UP_INVERSE_STEP_MIN) {
            ui8_duty_cycle_ramp_up_inverse_step = PWM_DUTY_CYCLE_RAMP_UP_INVERSE_STEP_MIN;
        }

        // limit target duty cycle ramp down inverse step if lower than min value (safety)
        if (ui8_duty_cycle_ramp_down_inverse_step < PWM_DUTY_CYCLE_RAMP_DOWN_INVERSE_STEP_MIN) {
            ui8_duty_cycle_ramp_down_inverse_step = PWM_DUTY_CYCLE_RAMP_DOWN_INVERSE_STEP_MIN;
        }
		
        // set duty cycle ramp up in controller
        ui8_controller_duty_cycle_ramp_up_inverse_step = ui8_duty_cycle_ramp_up_inverse_step;

        // set duty cycle ramp down in controller
        ui8_controller_duty_cycle_ramp_down_inverse_step = ui8_duty_cycle_ramp_down_inverse_step;

        // set target battery current in controller
        ui8_controller_adc_battery_current_target = ui8_adc_battery_current_target;

        // set target duty cycle in controller
        ui8_controller_duty_cycle_target = ui8_duty_cycle_target;
	}
}

// calculate motor torque rates up/down depending on speed and cadence
static void set_assist_torque_rates(void){
	uint8_t ui8_tmp;
	if (ui16_wheel_speed_x10 >= 200) {
        ui8_duty_cycle_ramp_up_inverse_step = PWM_DUTY_CYCLE_RAMP_UP_INVERSE_STEP_MIN;
        ui8_duty_cycle_ramp_down_inverse_step = PWM_DUTY_CYCLE_RAMP_DOWN_INVERSE_STEP_MIN;
    } else {
        ui8_duty_cycle_ramp_up_inverse_step = map_ui8((uint8_t)(ui16_wheel_speed_x10>>2),
                (uint8_t)10, // 10*4 = 40 -> 4 kph
                (uint8_t)50, // 50*4 = 200 -> 20 kph
                (uint8_t)ui8_duty_cycle_ramp_up_inverse_step_default,
                (uint8_t)PWM_DUTY_CYCLE_RAMP_UP_INVERSE_STEP_MIN);
        ui8_tmp = map_ui8(ui8_pedal_cadence_RPM,
                (uint8_t)20, // 20 rpm
                (uint8_t)70, // 70 rpm
                (uint8_t)ui8_duty_cycle_ramp_up_inverse_step_default,
                (uint8_t)PWM_DUTY_CYCLE_RAMP_UP_INVERSE_STEP_MIN);
        if (ui8_tmp < ui8_duty_cycle_ramp_up_inverse_step)
            ui8_duty_cycle_ramp_up_inverse_step = ui8_tmp;

        ui8_duty_cycle_ramp_down_inverse_step = map_ui8((uint8_t)(ui16_wheel_speed_x10>>2),
                (uint8_t)10, // 10*4 = 40 -> 4 kph
                (uint8_t)50, // 50*4 = 200 -> 20 kph
                (uint8_t)ui8_duty_cycle_ramp_down_inverse_step_default,
                (uint8_t)PWM_DUTY_CYCLE_RAMP_DOWN_INVERSE_STEP_MIN);
        ui8_tmp = map_ui8(ui8_pedal_cadence_RPM,
                (uint8_t)20, // 20 rpm
                (uint8_t)70, // 70 rpm
                (uint8_t)ui8_duty_cycle_ramp_down_inverse_step_default,
                (uint8_t)PWM_DUTY_CYCLE_RAMP_DOWN_INVERSE_STEP_MIN);
        if (ui8_tmp < ui8_duty_cycle_ramp_down_inverse_step)
            ui8_duty_cycle_ramp_down_inverse_step = ui8_tmp;
    }
}


static void apply_power_assist(void)
{
    uint8_t ui8_power_assist_multiplier_x50 = ui8_riding_mode_parameter;
	
	// check for assist without pedal rotation when there is no pedal rotation
	if(m_configuration_variables.ui8_assist_without_pedal_rotation_enabled) {
		if((!ui8_pedal_cadence_RPM) &&
		   (ui16_adc_pedal_torque_delta > (120 - ui8_assist_without_pedal_rotation_threshold))) {
				ui8_pedal_cadence_RPM = 1;
		}
	}
	
  if((ui8_pedal_cadence_RPM)||(ui8_startup_assist_adc_battery_current_target)) {
	// calculate torque on pedals + torque startup boost
    uint32_t ui32_pedal_torque_x100 = (uint32_t)(ui16_adc_pedal_torque_delta * ui8_pedal_torque_per_10_bit_ADC_step_x100);
	
    // calculate power assist by multiplying human power with the power assist multiplier
    uint32_t ui32_power_assist_x100 = (((uint32_t)(ui8_pedal_cadence_RPM * ui8_power_assist_multiplier_x50))
            * ui32_pedal_torque_x100) / 480U; // see note below

    /*------------------------------------------------------------------------

     NOTE: regarding the human power calculation

     (1) Formula: pedal power = torque * rotations per second * 2 * pi
     (2) Formula: pedal power = torque * rotations per minute * 2 * pi / 60
     (3) Formula: pedal power = torque * rotations per minute * 0.1047
     (4) Formula: pedal power = torque * 100 * rotations per minute * 0.001047
     (5) Formula: pedal power = torque * 100 * rotations per minute / 955
     (6) Formula: pedal power * 100  =  torque * 100 * rotations per minute * (100 / 955)
     (7) Formula: assist power * 100  =  torque * 100 * rotations per minute * (100 / 955) * (ui8_power_assist_multiplier_x50 / 50)
     (8) Formula: assist power * 100  =  torque * 100 * rotations per minute * (2 / 955) * ui8_power_assist_multiplier_x50
     (9) Formula: assist power * 100  =  torque * 100 * rotations per minute * ui8_power_assist_multiplier_x50 / 480

     ------------------------------------------------------------------------*/

    // calculate target current
    uint32_t ui32_battery_current_target_x100 = (ui32_power_assist_x100 * 1000) / ui16_battery_voltage_filtered_x1000;
	
    // set battery current target in ADC steps
    uint16_t ui16_adc_battery_current_target = (uint16_t)ui32_battery_current_target_x100 / BATTERY_CURRENT_PER_10_BIT_ADC_STEP_X100;
	    
	// set motor torque rate
	set_assist_torque_rates();

    // set battery current target
    if (ui16_adc_battery_current_target > ui8_adc_battery_current_max) {
        ui8_adc_battery_current_target = ui8_adc_battery_current_max;
    } else {
        ui8_adc_battery_current_target = ui16_adc_battery_current_target;
    }
	
	// set startup assist battery current target
	if (ui8_startup_assist) {
		if (ui8_adc_battery_current_target > ui8_startup_assist_adc_battery_current_target)
			ui8_startup_assist_adc_battery_current_target = ui8_adc_battery_current_target;
		
		ui8_adc_battery_current_target = ui8_startup_assist_adc_battery_current_target;
	} else {
		ui8_startup_assist_adc_battery_current_target = 0;
    }
	
    // set duty cycle target
    if (ui8_adc_battery_current_target) {
        ui8_duty_cycle_target = PWM_DUTY_CYCLE_MAX;
    } else {
        ui8_duty_cycle_target = 0;
    }
  }
}


static void apply_torque_assist(void)
{
	// check for assist without pedal rotation when there is no pedal rotation
	if(m_configuration_variables.ui8_assist_without_pedal_rotation_enabled) {
		if((!ui8_pedal_cadence_RPM)&&
			(ui16_adc_pedal_torque_delta > (120 - ui8_assist_without_pedal_rotation_threshold))) {
				ui8_pedal_cadence_RPM = 1;
		}
	}
	
	if(m_configuration_variables.ui8_assist_with_error_enabled)
		ui8_pedal_cadence_RPM = 1;
	
    // calculate torque assistance
    if (((ui16_adc_pedal_torque_delta)&&(ui8_pedal_cadence_RPM))
	  ||(ui8_startup_assist_adc_battery_current_target)) {
        // get the torque assist factor
        uint8_t ui8_torque_assist_factor = ui8_riding_mode_parameter;

        // calculate torque assist target current
        uint16_t ui16_adc_battery_current_target_torque_assist = ((uint16_t) ui16_adc_pedal_torque_delta
                * ui8_torque_assist_factor) / TORQUE_ASSIST_FACTOR_DENOMINATOR;

        // set motor torque rate
		set_assist_torque_rates();

        // set battery current target
        if (ui16_adc_battery_current_target_torque_assist > ui8_adc_battery_current_max) {
            ui8_adc_battery_current_target = ui8_adc_battery_current_max;
        } else {
            ui8_adc_battery_current_target = ui16_adc_battery_current_target_torque_assist;
        }
		
		// set startup assist battery current target
		if (ui8_startup_assist) {
			if (ui8_adc_battery_current_target > ui8_startup_assist_adc_battery_current_target)
				ui8_startup_assist_adc_battery_current_target = ui8_adc_battery_current_target;
		
			ui8_adc_battery_current_target = ui8_startup_assist_adc_battery_current_target;
		} else {
			ui8_startup_assist_adc_battery_current_target = 0;
		}
		
		// set duty cycle target
        if (ui8_adc_battery_current_target) {
            ui8_duty_cycle_target = PWM_DUTY_CYCLE_MAX;
        } else {
            ui8_duty_cycle_target = 0;
        }
    }
}


static void apply_cadence_assist(void)
{
    if (ui8_pedal_cadence_RPM) {
		// get the cadence assist duty cycle increment
		uint16_t ui16_cadence_assist_duty_cycle_increment = ((PWM_DUTY_CYCLE_MAX - ui8_riding_mode_parameter) * ui8_pedal_cadence_RPM) / 120;
        // get the cadence assist duty cycle target
		uint8_t ui8_cadence_assist_duty_cycle_target = ui8_riding_mode_parameter + (uint8_t)ui16_cadence_assist_duty_cycle_increment;
		
        // set motor torque rate
		set_assist_torque_rates();

        // set battery current target
        ui8_adc_battery_current_target = ui8_adc_battery_current_max;

        // set duty cycle target
        ui8_duty_cycle_target = ui8_cadence_assist_duty_cycle_target;
    }
}


static void apply_emtb_assist(void)
{
//#define eMTB_ASSIST_ADC_TORQUE_OFFSET    10
#define eMTB_ASSIST_ADC_TORQUE_OFFSET    5
	
	// check for assist without pedal rotation when there is no pedal rotation
	if(m_configuration_variables.ui8_assist_without_pedal_rotation_enabled) {
		if((!ui8_pedal_cadence_RPM)&&
			(ui16_adc_pedal_torque_delta > (120 - ui8_assist_without_pedal_rotation_threshold))) {
				ui8_pedal_cadence_RPM = 1;
		}
	}

	if(m_configuration_variables.ui8_assist_with_error_enabled)
		ui8_pedal_cadence_RPM = 1;
	
	if(((ui16_adc_pedal_torque_delta > 0) && 
		(ui16_adc_pedal_torque_delta < (eMTB_POWER_FUNCTION_ARRAY_SIZE - eMTB_ASSIST_ADC_TORQUE_OFFSET)) &&
		(ui8_pedal_cadence_RPM))||(ui8_startup_assist_adc_battery_current_target))
	{
		// initialize eMTB assist target current
		uint8_t ui8_adc_battery_current_target_eMTB_assist = 0;
		
		// get the eMTB assist sensitivity
		uint8_t ui8_eMTB_assist_sensitivity = ui8_riding_mode_parameter;
		
		switch (ui8_eMTB_assist_sensitivity)
		{
			case 0: ui8_adc_battery_current_target_eMTB_assist = 0;
			case 1: ui8_adc_battery_current_target_eMTB_assist = ui8_eMTB_power_function_160[ui16_adc_pedal_torque_delta + eMTB_ASSIST_ADC_TORQUE_OFFSET]; break;
			case 2: ui8_adc_battery_current_target_eMTB_assist = ui8_eMTB_power_function_165[ui16_adc_pedal_torque_delta + eMTB_ASSIST_ADC_TORQUE_OFFSET]; break;
			case 3: ui8_adc_battery_current_target_eMTB_assist = ui8_eMTB_power_function_170[ui16_adc_pedal_torque_delta + eMTB_ASSIST_ADC_TORQUE_OFFSET]; break;
			case 4: ui8_adc_battery_current_target_eMTB_assist = ui8_eMTB_power_function_175[ui16_adc_pedal_torque_delta + eMTB_ASSIST_ADC_TORQUE_OFFSET]; break;
			case 5: ui8_adc_battery_current_target_eMTB_assist = ui8_eMTB_power_function_180[ui16_adc_pedal_torque_delta + eMTB_ASSIST_ADC_TORQUE_OFFSET]; break;
			case 6: ui8_adc_battery_current_target_eMTB_assist = ui8_eMTB_power_function_185[ui16_adc_pedal_torque_delta + eMTB_ASSIST_ADC_TORQUE_OFFSET]; break;
			case 7: ui8_adc_battery_current_target_eMTB_assist = ui8_eMTB_power_function_190[ui16_adc_pedal_torque_delta + eMTB_ASSIST_ADC_TORQUE_OFFSET]; break;
			case 8: ui8_adc_battery_current_target_eMTB_assist = ui8_eMTB_power_function_195[ui16_adc_pedal_torque_delta + eMTB_ASSIST_ADC_TORQUE_OFFSET]; break;
			case 9: ui8_adc_battery_current_target_eMTB_assist = ui8_eMTB_power_function_200[ui16_adc_pedal_torque_delta + eMTB_ASSIST_ADC_TORQUE_OFFSET]; break;
			case 10: ui8_adc_battery_current_target_eMTB_assist = ui8_eMTB_power_function_205[ui16_adc_pedal_torque_delta + eMTB_ASSIST_ADC_TORQUE_OFFSET]; break;
			case 11: ui8_adc_battery_current_target_eMTB_assist = ui8_eMTB_power_function_210[ui16_adc_pedal_torque_delta + eMTB_ASSIST_ADC_TORQUE_OFFSET]; break;
			case 12: ui8_adc_battery_current_target_eMTB_assist = ui8_eMTB_power_function_215[ui16_adc_pedal_torque_delta + eMTB_ASSIST_ADC_TORQUE_OFFSET]; break;
			case 13: ui8_adc_battery_current_target_eMTB_assist = ui8_eMTB_power_function_220[ui16_adc_pedal_torque_delta + eMTB_ASSIST_ADC_TORQUE_OFFSET]; break;
			case 14: ui8_adc_battery_current_target_eMTB_assist = ui8_eMTB_power_function_225[ui16_adc_pedal_torque_delta + eMTB_ASSIST_ADC_TORQUE_OFFSET]; break;
			case 15: ui8_adc_battery_current_target_eMTB_assist = ui8_eMTB_power_function_230[ui16_adc_pedal_torque_delta + eMTB_ASSIST_ADC_TORQUE_OFFSET]; break;
			case 16: ui8_adc_battery_current_target_eMTB_assist = ui8_eMTB_power_function_235[ui16_adc_pedal_torque_delta + eMTB_ASSIST_ADC_TORQUE_OFFSET]; break;
			case 17: ui8_adc_battery_current_target_eMTB_assist = ui8_eMTB_power_function_240[ui16_adc_pedal_torque_delta + eMTB_ASSIST_ADC_TORQUE_OFFSET]; break;
			case 18: ui8_adc_battery_current_target_eMTB_assist = ui8_eMTB_power_function_245[ui16_adc_pedal_torque_delta + eMTB_ASSIST_ADC_TORQUE_OFFSET]; break;
			case 19: ui8_adc_battery_current_target_eMTB_assist = ui8_eMTB_power_function_250[ui16_adc_pedal_torque_delta + eMTB_ASSIST_ADC_TORQUE_OFFSET]; break;
			case 20: ui8_adc_battery_current_target_eMTB_assist = ui8_eMTB_power_function_255[ui16_adc_pedal_torque_delta + eMTB_ASSIST_ADC_TORQUE_OFFSET]; break;
		}

        // set motor torque rate
		set_assist_torque_rates();

        // set battery current target
        if (ui8_adc_battery_current_target_eMTB_assist > ui8_adc_battery_current_max) {
            ui8_adc_battery_current_target = ui8_adc_battery_current_max;
        } else {
            ui8_adc_battery_current_target = ui8_adc_battery_current_target_eMTB_assist;
        }
		
		// set startup assist battery current target
		if (ui8_startup_assist) {
			if (ui8_adc_battery_current_target > ui8_startup_assist_adc_battery_current_target)
				ui8_startup_assist_adc_battery_current_target = ui8_adc_battery_current_target;
		
			ui8_adc_battery_current_target = ui8_startup_assist_adc_battery_current_target;
		} else {
			ui8_startup_assist_adc_battery_current_target = 0;
		}
		
        // set duty cycle target
        if (ui8_adc_battery_current_target) {
            ui8_duty_cycle_target = PWM_DUTY_CYCLE_MAX;
        } else {
            ui8_duty_cycle_target = 0;
        }
    }
}


static void apply_walk_assist(void)
{
	if(m_configuration_variables.ui8_assist_with_error_enabled) {
		// get walk assist duty cycle target
		ui8_walk_assist_duty_cycle_target = ui8_riding_mode_parameter + 20;
	}
	else {
		// get walk assist speed target x10
		ui8_walk_assist_speed_target_x10 = ui8_riding_mode_parameter;
			
		// set walk assist duty cycle target
		if((!ui8_walk_assist_speed_flag)&&(!ui16_motor_speed_erps)) {
			ui8_walk_assist_duty_cycle_target = WALK_ASSIST_DUTY_CYCLE_STARTUP;
			ui8_walk_assist_duty_cycle_max = WALK_ASSIST_DUTY_CYCLE_STARTUP;
			ui16_walk_assist_wheel_speed_counter = 0;
			ui16_walk_assist_erps_target = 0;
		}
		else if(ui8_walk_assist_speed_flag) {
			if(ui16_motor_speed_erps < ui16_walk_assist_erps_min) {
				ui8_walk_assist_adj_delay = WALK_ASSIST_ADJ_DELAY_MIN;
				
				if(ui8_walk_assist_duty_cycle_counter++ > ui8_walk_assist_adj_delay) {
					if(ui8_walk_assist_duty_cycle_target < ui8_walk_assist_duty_cycle_max) {
						ui8_walk_assist_duty_cycle_target++;
					}
					else {
						ui8_walk_assist_duty_cycle_max++;
					}
					ui8_walk_assist_duty_cycle_counter = 0;
				}
			}
			else if(ui16_motor_speed_erps < ui16_walk_assist_erps_target) {
				ui8_walk_assist_adj_delay = (ui16_motor_speed_erps - ui16_walk_assist_erps_min) * WALK_ASSIST_ADJ_DELAY_MIN;
				
				if(ui8_walk_assist_duty_cycle_counter++ > ui8_walk_assist_adj_delay) {
					if(ui8_walk_assist_duty_cycle_target < ui8_walk_assist_duty_cycle_max) {
						ui8_walk_assist_duty_cycle_target++;
					}
					
					ui8_walk_assist_duty_cycle_counter = 0;
				}
			}
			else if(ui16_motor_speed_erps < ui16_walk_assist_erps_max) {
				ui8_walk_assist_adj_delay = (ui16_walk_assist_erps_max - ui16_motor_speed_erps) * WALK_ASSIST_ADJ_DELAY_MIN;
			
				if(ui8_walk_assist_duty_cycle_counter++ > ui8_walk_assist_adj_delay) {
					if(ui8_walk_assist_duty_cycle_target > WALK_ASSIST_DUTY_CYCLE_MIN) {
						ui8_walk_assist_duty_cycle_target--;
					}
					ui8_walk_assist_duty_cycle_counter = 0;
				}
			}
			else if(ui16_motor_speed_erps >= ui16_walk_assist_erps_max) {
				ui8_walk_assist_adj_delay = WALK_ASSIST_ADJ_DELAY_MIN;
			
				if(ui8_walk_assist_duty_cycle_counter++ > ui8_walk_assist_adj_delay) {
					if(ui8_walk_assist_duty_cycle_target > WALK_ASSIST_DUTY_CYCLE_MIN) {
						ui8_walk_assist_duty_cycle_target--;
						ui8_walk_assist_duty_cycle_max--;
					}
					ui8_walk_assist_duty_cycle_counter = 0;
				}
			}
		}
		else {
			ui8_walk_assist_adj_delay = WALK_ASSIST_ADJ_DELAY_STARTUP;
			
			if(ui8_walk_assist_duty_cycle_counter++ > ui8_walk_assist_adj_delay) {
				if(ui16_wheel_speed_x10) {
					if(ui16_wheel_speed_x10 > 42) {
						ui8_walk_assist_duty_cycle_target--;
					}
					
					if(ui16_walk_assist_wheel_speed_counter++ >= 10) {
						ui8_walk_assist_duty_cycle_max += 10;
					
						// set walk assist erps target
						ui16_walk_assist_erps_target = ((ui16_motor_speed_erps * ui8_walk_assist_speed_target_x10) / ui16_wheel_speed_x10);
						ui16_walk_assist_erps_min = ui16_walk_assist_erps_target - WALK_ASSIST_ERPS_THRESHOLD;
						ui16_walk_assist_erps_max = ui16_walk_assist_erps_target + WALK_ASSIST_ERPS_THRESHOLD;
					
						// set walk assist speed flag
						ui8_walk_assist_speed_flag = 1;
					}
				}
				else {
					if((ui8_walk_assist_duty_cycle_max + 10) < WALK_ASSIST_DUTY_CYCLE_MAX) {
						ui8_walk_assist_duty_cycle_target++;
						ui8_walk_assist_duty_cycle_max++;
					}
				}
				ui8_walk_assist_duty_cycle_counter = 0;
			}
		}
	}

	if(ui8_walk_assist_duty_cycle_target > WALK_ASSIST_DUTY_CYCLE_MAX) {
		ui8_walk_assist_duty_cycle_target = WALK_ASSIST_DUTY_CYCLE_MAX;
	}
	
	// set motor acceleration
	ui8_duty_cycle_ramp_up_inverse_step = WALK_ASSIST_DUTY_CYCLE_RAMP_UP_INVERSE_STEP;	
	ui8_duty_cycle_ramp_down_inverse_step = PWM_DUTY_CYCLE_RAMP_DOWN_INVERSE_STEP_DEFAULT;
	
	if(ui16_wheel_speed_x10 < WALK_ASSIST_THRESHOLD_SPEED_X10) {
		// set battery current target
		ui8_adc_battery_current_target = ui8_min(WALK_ASSIST_ADC_BATTERY_CURRENT_MAX, ui8_adc_battery_current_max);
		// set duty cycle target
		ui8_duty_cycle_target = ui8_walk_assist_duty_cycle_target;
	}
	else {
		ui8_adc_battery_current_target = 0;
		ui8_duty_cycle_target = 0;
	}
}


static void apply_cruise(void)
{
//#define CRUISE_PID_KP                             14    // 48 volt motor: 12, 36 volt motor: 14
//#define CRUISE_PID_KI                             0.7   // 48 volt motor: 1, 36 volt motor: 0.7
#define CRUISE_PID_INTEGRAL_LIMIT                 1000
#define CRUISE_PID_KD                             0
	
	static uint8_t ui8_riding_mode_parameter_cruise = 0;
	static uint8_t ui8_riding_mode_cruise = 0;
	static uint8_t ui8_riding_mode_cruise_temp = 0;
	uint8_t ui8_cruise_threshold_speed_x10;
	
	if(m_configuration_variables.ui8_street_mode_enabled)
		ui8_cruise_threshold_speed_x10 = ui8_cruise_threshold_speed_x10_street;
	else
		ui8_cruise_threshold_speed_x10 = ui8_cruise_threshold_speed_x10_offroad;
	
	// verify speed target change
	if(ui8_riding_mode_parameter_cruise != ui8_riding_mode_parameter)
		// restart cruise counter
		ui8_cruise_counter = 0;
			
	// for next verify speed target change
	ui8_riding_mode_parameter_cruise = ui8_riding_mode_parameter;
	  
	// verify riding mode change
	if(ui8_riding_mode_cruise_temp != ui8_riding_mode_cruise)
		// for next verify riding mode change
		ui8_riding_mode_cruise_temp = ui8_riding_mode_cruise;

	#if STREET_MODE_CRUISE_ENABLED
		#if CRUISE_MODE_WALK_ENABLED
			#if ENABLE_BRAKE_SENSOR
				if((ui16_wheel_speed_x10 >= ui8_cruise_threshold_speed_x10)&&(ui8_cruise_counter > 1)&&((ui8_pedal_cadence_RPM)||(ui8_cruise_button_flag)))
			#else
				if((ui16_wheel_speed_x10 >= ui8_cruise_threshold_speed_x10)&&(ui8_cruise_counter > 1)&&(ui8_pedal_cadence_RPM))
			#endif
		#else
			if((ui16_wheel_speed_x10 >= ui8_cruise_threshold_speed_x10)&&(ui8_cruise_counter > 1)&&(ui8_pedal_cadence_RPM))
		#endif
	#else
		#if CRUISE_MODE_WALK_ENABLED
			#if ENABLE_BRAKE_SENSOR
			if((ui16_wheel_speed_x10 >= ui8_cruise_threshold_speed_x10)&&(ui8_cruise_counter > 1)&&(!m_configuration_variables.ui8_street_mode_enabled)&&((ui8_pedal_cadence_RPM)||(ui8_cruise_button_flag)))
			#else
			if((ui16_wheel_speed_x10 >= ui8_cruise_threshold_speed_x10)&&(ui8_cruise_counter > 1)&&(!m_configuration_variables.ui8_street_mode_enabled)&&(ui8_pedal_cadence_RPM))
			#endif
		#else
			if((ui16_wheel_speed_x10 >= ui8_cruise_threshold_speed_x10)&&(ui8_cruise_counter > 1)&&(!m_configuration_variables.ui8_street_mode_enabled)&&(ui8_pedal_cadence_RPM))
		#endif
	#endif

	{		  
		static int16_t i16_error;
		static int16_t i16_last_error;
		static int16_t i16_integral;
		static int16_t i16_derivative;
		static int16_t i16_control_output;
		static uint16_t ui16_wheel_speed_target_x10;
	
		// for verify riding mode change
		ui8_riding_mode_cruise = CRUISE_MODE;
		
		// initialize cruise PID controller
		if (ui8_cruise_PID_initialize)
		{
			ui8_cruise_PID_initialize = 0;
		  
			// reset PID variables
			i16_error = 0;
			i16_last_error = 0;
			i16_integral = 320; // initialize integral to a value so the motor does not start from zero
			i16_derivative = 0;
			i16_control_output = 0;
		  
			// check what target wheel speed to use (received or current)
			uint16_t ui16_wheel_speed_target_received_x10 = (uint16_t) ui8_riding_mode_parameter * 10;
		  
			if (ui16_wheel_speed_target_received_x10 > 0)
			{
				// set received target wheel speed to target wheel speed
				ui16_wheel_speed_target_x10 = ui16_wheel_speed_target_received_x10;
			}
			else
			{
				// set current wheel speed to maintain
				ui16_wheel_speed_target_x10 = ui16_wheel_speed_x10;
			}
		}
		
		// calculate error
		i16_error = (ui16_wheel_speed_target_x10 - ui16_wheel_speed_x10);
		
		// calculate integral
		i16_integral = i16_integral + i16_error;
		
		// limit integral
		if (i16_integral > CRUISE_PID_INTEGRAL_LIMIT)
		{
			i16_integral = CRUISE_PID_INTEGRAL_LIMIT; 
		}
		else if (i16_integral < 0)
		{
			i16_integral = 0;
		}
		
		// calculate derivative
		i16_derivative = i16_error - i16_last_error;

		// save error to last error
		i16_last_error = i16_error;

		// calculate control output ( output =  P I D )
		i16_control_output = (CRUISE_PID_KP * i16_error) + (CRUISE_PID_KI * i16_integral) + (CRUISE_PID_KD * i16_derivative);
		
		// limit control output to just positive values
		if (i16_control_output < 0) { i16_control_output = 0; }
		
		// limit control output to the maximum value
		if (i16_control_output > 1000) { i16_control_output = 1000; }
		
		// set motor acceleration
        ui8_duty_cycle_ramp_up_inverse_step = CRUISE_DUTY_CYCLE_RAMP_UP_INVERSE_STEP;
        ui8_duty_cycle_ramp_down_inverse_step = PWM_DUTY_CYCLE_RAMP_DOWN_INVERSE_STEP_DEFAULT;
		
		// set battery current target
		ui8_adc_battery_current_target = ui8_adc_battery_current_max;
		
		// set duty cycle target  |  map the control output to an appropriate target PWM value
		ui8_duty_cycle_target = map_ui8((uint8_t) (i16_control_output >> 2),
									(uint8_t) 0,                     // minimum control output from PID
									(uint8_t) 250,                   // maximum control output from PID
									(uint8_t) 0,                     // minimum duty cycle
									(uint8_t)(PWM_DUTY_CYCLE_MAX)); // maximum duty cycle
	}
	else
	{
		// for verify riding mode change
		ui8_riding_mode_cruise = CADENCE_ASSIST_MODE;
				
		// applies cadence assist up to cruise speed threshold
		ui8_riding_mode_parameter = ui8_riding_mode_parameter_array[CADENCE_ASSIST_MODE - 1][ui8_assist_level];
		apply_cadence_assist();
		
		// restore cruise riding mode parameter
		ui8_riding_mode_parameter = ui8_riding_mode_parameter_cruise;
		ui8_cruise_PID_initialize = 1;
	}
}


static void apply_hybrid_assist(void)
{
	uint16_t ui16_adc_battery_current_target_power_assist;
	uint16_t ui16_adc_battery_current_target_torque_assist;
	uint16_t ui16_adc_battery_current_target;
	
	// check for assist without pedal rotation when there is no pedal rotation
	if(m_configuration_variables.ui8_assist_without_pedal_rotation_enabled) {
		if((!ui8_pedal_cadence_RPM)&&
			(ui16_adc_pedal_torque_delta > (120 - ui8_assist_without_pedal_rotation_threshold))) {
				ui8_pedal_cadence_RPM = 1;
		}
	}
	
  if((ui8_pedal_cadence_RPM)||(ui8_startup_assist_adc_battery_current_target)) {
	// calculate torque assistance
	if (ui16_adc_pedal_torque_delta)
	{
		// get the torque assist factor
		uint8_t ui8_torque_assist_factor = ui8_riding_mode_parameter_array[TORQUE_ASSIST_MODE - 1][ui8_assist_level];
		
		// calculate torque assist target current
		ui16_adc_battery_current_target_torque_assist = ((uint16_t) ui16_adc_pedal_torque_delta * ui8_torque_assist_factor) / TORQUE_ASSIST_FACTOR_DENOMINATOR;
	}
	else
	{
		ui16_adc_battery_current_target_torque_assist = 0;
	}
	
	// calculate power assistance
	// get the power assist multiplier
	uint8_t ui8_power_assist_multiplier_x50 = ui8_riding_mode_parameter;

	// calculate power assist by multiplying human power with the power assist multiplier
    uint32_t ui32_power_assist_x100 = (((uint32_t)(ui8_pedal_cadence_RPM * ui8_power_assist_multiplier_x50))
            * ui16_pedal_torque_x100) / 480U; // see note below
	
	// calculate power assist target current x100
	uint32_t ui32_battery_current_target_x100 = (ui32_power_assist_x100 * 1000) / ui16_battery_voltage_filtered_x1000;
	
	// calculate power assist target current
	ui16_adc_battery_current_target_power_assist = (uint16_t)ui32_battery_current_target_x100 / BATTERY_CURRENT_PER_10_BIT_ADC_STEP_X100;
	
	// set battery current target in ADC steps
	if(ui16_adc_battery_current_target_power_assist > ui16_adc_battery_current_target_torque_assist)
		ui16_adc_battery_current_target = ui16_adc_battery_current_target_power_assist;
	else
		ui16_adc_battery_current_target = ui16_adc_battery_current_target_torque_assist;
	
	// set motor torque rate
	set_assist_torque_rates();
	
	// set battery current target
	if (ui16_adc_battery_current_target > ui8_adc_battery_current_max) { ui8_adc_battery_current_target = ui8_adc_battery_current_max; }
	else { ui8_adc_battery_current_target = ui16_adc_battery_current_target; }
	
	// set startup assist battery current target
	if (ui8_startup_assist) {
		if (ui8_adc_battery_current_target > ui8_startup_assist_adc_battery_current_target)
			ui8_startup_assist_adc_battery_current_target = ui8_adc_battery_current_target;
		
		ui8_adc_battery_current_target = ui8_startup_assist_adc_battery_current_target;
	} else {
		ui8_startup_assist_adc_battery_current_target = 0;
	}
	
	// set duty cycle target
	if (ui8_adc_battery_current_target) { ui8_duty_cycle_target = PWM_DUTY_CYCLE_MAX; }
	else { ui8_duty_cycle_target = 0; }
  }
}


static void apply_torque_sensor_calibration(void)
{
#define PEDAL_TORQUE_ADC_STEP_MIN_VALUE		160 // 20 << 3
#define PEDAL_TORQUE_ADC_STEP_MAX_VALUE		720 // 90 << 3
	
	static uint8_t ui8_step_counter;
	
	if(ui8_torque_sensor_calibration_started)
	{
		// increment pedal torque step temp
		if (ui8_step_counter++ & 0x01)
			ui16_pedal_torque_step_temp++;
			
		if(ui16_pedal_torque_step_temp > PEDAL_TORQUE_ADC_STEP_MAX_VALUE)
			ui16_pedal_torque_step_temp = PEDAL_TORQUE_ADC_STEP_MIN_VALUE;
		
		// calculate pedal torque 10 bit ADC step x100
		ui8_pedal_torque_per_10_bit_ADC_step_x100 = ui16_pedal_torque_step_temp >> 3;
			
		// pedal weight (from LCD3)
		ui16_pedal_weight_x100 = (uint16_t)(((uint32_t) ui16_pedal_torque_x100 * 100) / 167);
		//uint16_t ui16_adc_pedal_torque_delta_simulation = 100; // weight 20kg
		//uint16_t ui16_adc_pedal_torque_delta_simulation = 110; // weight 25kg
		//ui16_pedal_weight_x100 = (uint16_t)(((uint32_t) ui8_pedal_torque_per_10_bit_ADC_step_x100 * ui16_adc_pedal_torque_delta_simulation * 100) / 167);
	}
	else
	{	
		ui16_pedal_torque_step_temp = PEDAL_TORQUE_ADC_STEP_MIN_VALUE;
	}
}


static void apply_throttle(void)
{
    // map adc value from 0 to 255
    ui8_throttle_adc = map_ui8((uint8_t) UI8_ADC_THROTTLE,
            (uint8_t) ADC_THROTTLE_MIN_VALUE,
            (uint8_t) ADC_THROTTLE_MAX_VALUE,
            (uint8_t) 0,
            (uint8_t) 255);
	
	// throttle legal in street mode
	if((ui8_street_mode_throttle_legal)&&(!ui8_pedal_cadence_RPM)) {
		ui8_throttle_adc = 0;
	}
	
	// map ADC throttle value from 0 to max battery current
    uint8_t ui8_adc_battery_current_target_throttle = map_ui8((uint8_t) ui8_throttle_adc,
            (uint8_t) 0,
            (uint8_t) 255,
            (uint8_t) 0,
            (uint8_t) ui8_adc_battery_current_max);
			
    if (ui8_adc_battery_current_target_throttle > ui8_adc_battery_current_target) {
        // set motor acceleration
        if (ui16_wheel_speed_x10 >= 255) {
            ui8_duty_cycle_ramp_up_inverse_step = PWM_DUTY_CYCLE_RAMP_UP_INVERSE_STEP_MIN;
            ui8_duty_cycle_ramp_down_inverse_step = PWM_DUTY_CYCLE_RAMP_DOWN_INVERSE_STEP_MIN;
        } else {
            ui8_duty_cycle_ramp_up_inverse_step = map_ui8((uint8_t) ui16_wheel_speed_x10,
                    (uint8_t) 40,
                    (uint8_t) 255,
                    (uint8_t) THROTTLE_DUTY_CYCLE_RAMP_UP_INVERSE_STEP_DEFAULT,
                    (uint8_t) THROTTLE_DUTY_CYCLE_RAMP_UP_INVERSE_STEP_MIN);

            ui8_duty_cycle_ramp_down_inverse_step = map_ui8((uint8_t) ui16_wheel_speed_x10,
                    (uint8_t) 40,
                    (uint8_t) 255,
                    (uint8_t) PWM_DUTY_CYCLE_RAMP_DOWN_INVERSE_STEP_DEFAULT,
                    (uint8_t) PWM_DUTY_CYCLE_RAMP_DOWN_INVERSE_STEP_MIN);
        }
        // set battery current target
        ui8_adc_battery_current_target = ui8_adc_battery_current_target_throttle;

        // set duty cycle target
        ui8_duty_cycle_target = PWM_DUTY_CYCLE_MAX;
    }
}


static void apply_temperature_limiting(void)
{
    // get ADC measurement
    uint16_t ui16_temp = UI16_ADC_10_BIT_THROTTLE;

    // filter ADC measurement to motor temperature variable
    ui16_adc_motor_temperature_filtered = filter(ui16_temp, ui16_adc_motor_temperature_filtered, 8);

    // convert ADC value
    ui16_motor_temperature_filtered_x10 = (uint16_t)(((uint32_t) ui16_adc_motor_temperature_filtered * 10000) / 2048);

    // min temperature value can not be equal or higher than max temperature value
    if (ui8_motor_temperature_min_value_to_limit >= ui8_motor_temperature_max_value_to_limit) {
        ui8_adc_battery_current_target = 0;
    } else {
        // adjust target current if motor over temperature limit
        ui8_adc_battery_current_target = map_ui16((uint16_t) ui16_motor_temperature_filtered_x10,
				(uint16_t) ((uint8_t)ui8_motor_temperature_min_value_to_limit * (uint8_t)10U),
				(uint16_t) ((uint8_t)ui8_motor_temperature_max_value_to_limit * (uint8_t)10U),
				ui8_adc_battery_current_target,
				0);
	}
}


static void apply_speed_limit(void)
{
    if (m_configuration_variables.ui8_wheel_speed_max > 0) {
        // set battery current target
        ui8_adc_battery_current_target = map_ui16((uint16_t) ui16_wheel_speed_x10,
                (uint16_t) (((uint8_t)(m_configuration_variables.ui8_wheel_speed_max) * (uint8_t)10U) - (uint8_t)20U),
                (uint16_t) (((uint8_t)(m_configuration_variables.ui8_wheel_speed_max) * (uint8_t)10U) + (uint8_t)20U),
                ui8_adc_battery_current_target,
                0);
    }
}


static void calc_wheel_speed(void)
{
    // calc wheel speed (km/h x10)
    if (ui16_wheel_speed_sensor_ticks) {
        uint16_t ui16_tmp = ui16_wheel_speed_sensor_ticks;
        // rps = PWM_CYCLES_SECOND / ui16_wheel_speed_sensor_ticks (rev/sec)
        // km/h*10 = rps * ui16_wheel_perimeter * ((3600 / (1000 * 1000)) * 10)
        // !!!warning if PWM_CYCLES_SECOND is not a multiple of 1000
        ui16_wheel_speed_x10 = (uint16_t)(((uint32_t) m_configuration_variables.ui16_wheel_perimeter * ((PWM_CYCLES_SECOND/1000)*36U)) / ui16_tmp);
    } else {
        ui16_wheel_speed_x10 = 0;
    }
}


static void calc_cadence(void)
{
    // get the cadence sensor ticks
    uint16_t ui16_cadence_sensor_ticks_temp = ui16_cadence_sensor_ticks;

    // adjust cadence sensor ticks counter min depending on wheel speed
    ui16_cadence_ticks_count_min_speed_adj = map_ui16(ui16_wheel_speed_x10,
            40,
            400,
            CADENCE_SENSOR_CALC_COUNTER_MIN,
            CADENCE_SENSOR_TICKS_COUNTER_MIN_AT_SPEED);

    // calculate cadence in RPM and avoid zero division
    // !!!warning if PWM_CYCLES_SECOND > 21845
    if (ui16_cadence_sensor_ticks_temp) {
        ui8_pedal_cadence_RPM = (uint8_t)((PWM_CYCLES_SECOND * 3U) / ui16_cadence_sensor_ticks_temp);
		
		if(ui8_pedal_cadence_RPM > 120) {ui8_pedal_cadence_RPM = 120;}
	}
    else {
        ui8_pedal_cadence_RPM = 0;
	}
	
	/*-------------------------------------------------------------------------------------------------

     NOTE: regarding the cadence calculation

     Cadence is calculated by counting how many ticks there are between two LOW to HIGH transitions.

     Formula for calculating the cadence in RPM:

     (1) Cadence in RPM = (60 * PWM_CYCLES_SECOND) / CADENCE_SENSOR_NUMBER_MAGNETS) / ticks

     (2) Cadence in RPM = (PWM_CYCLES_SECOND * 3) / ticks

     -------------------------------------------------------------------------------------------------*/
}


static void get_battery_voltage_filtered(void)
{
    ui16_battery_voltage_filtered_x1000 = ui16_adc_battery_voltage_filtered * BATTERY_VOLTAGE_PER_10_BIT_ADC_STEP_X1000;
}


static void get_battery_current_filtered(void)
{
	ui8_battery_current_filtered_x10 = (uint8_t)(((uint16_t) ui8_adc_battery_current_filtered * BATTERY_CURRENT_PER_10_BIT_ADC_STEP_X100) / 10);
}

#define TOFFSET_CYCLES 120 // 3sec (25ms*120)
static uint8_t toffset_cycle_counter = 0;

static void get_pedal_torque(void)
{
	uint16_t ui16_temp = 0;
	
    if(toffset_cycle_counter < TOFFSET_CYCLES) {
        uint16_t ui16_tmp = UI16_ADC_10_BIT_TORQUE_SENSOR;
        ui16_adc_pedal_torque_offset_init = filter(ui16_tmp, ui16_adc_pedal_torque_offset_init, 2);
        toffset_cycle_counter++;
		
		// check the offset calibration
		if((toffset_cycle_counter == TOFFSET_CYCLES)&&(ui8_torque_sensor_calibrated)) {
			if((ui16_adc_pedal_torque_offset_init > ui16_adc_pedal_torque_offset_min)&& 
			  (ui16_adc_pedal_torque_offset_init < ui16_adc_pedal_torque_offset_max))
				ui8_adc_pedal_torque_offset_error = 0;
			else
				ui8_adc_pedal_torque_offset_error = 1;
		}
		
        ui16_adc_pedal_torque = ui16_adc_pedal_torque_offset_init;
		ui16_adc_pedal_torque_offset_cal = ui16_adc_pedal_torque_offset_init + ADC_TORQUE_SENSOR_CALIBRATION_OFFSET;
	}
	else {
		// torque sensor offset adjustment
		ui16_adc_pedal_torque_offset = ui16_adc_pedal_torque_offset_cal;
		if(ui8_pedal_cadence_RPM) {
			ui16_adc_pedal_torque_offset -= ADC_TORQUE_SENSOR_MIDDLE_OFFSET_ADJ;
			ui16_adc_pedal_torque_offset += ADC_TORQUE_SENSOR_OFFSET_ADJ;
		}
		
		// calculate coaster brake threshold
		#if COASTER_BRAKE_ENABLED
		if (ui16_adc_pedal_torque_offset > COASTER_BRAKE_TORQUE_THRESHOLD) {
			//ui16_adc_coaster_brake_threshold = ui16_adc_pedal_torque_offset - COASTER_BRAKE_TORQUE_THRESHOLD;
			ui16_adc_coaster_brake_threshold = ui16_adc_pedal_torque_offset_cal - COASTER_BRAKE_TORQUE_THRESHOLD;
		}
		else {
			ui16_adc_coaster_brake_threshold = 0;
		}
		#endif
		
        // get adc pedal torque
        ui16_adc_pedal_torque = UI16_ADC_10_BIT_TORQUE_SENSOR;
    }
	
    // calculate the delta value of adc pedal torque and the adc pedal torque range from calibration
    if(ui16_adc_pedal_torque > ui16_adc_pedal_torque_offset) {
		// adc pedal torque delta remapping
		if((ui8_torque_sensor_calibrated)&&(m_configuration_variables.ui8_torque_sensor_adv_enabled)) {
			// adc pedal torque delta adjustment
			ui16_temp = ui16_adc_pedal_torque - ui16_adc_pedal_torque_offset_init;
			ui16_adc_pedal_torque_delta = ui16_adc_pedal_torque - ui16_adc_pedal_torque_offset
				- ((ADC_TORQUE_SENSOR_DELTA_ADJ	* ui16_temp) / ADC_TORQUE_SENSOR_RANGE_TARGET);
			
			// adc pedal torque range adjusment
			ui16_temp = (ui16_adc_pedal_torque_delta * ADC_TORQUE_SENSOR_RANGE_INGREASE_X100) / 10;
			ui16_adc_pedal_torque_delta = (((ui16_temp
				* ((ui16_temp / ADC_TORQUE_SENSOR_ANGLE_COEFF + ADC_TORQUE_SENSOR_ANGLE_COEFF_X10) / ADC_TORQUE_SENSOR_ANGLE_COEFF)) / 50) // 100
				* (100 + PEDAL_TORQUE_ADC_RANGE_ADJ)) / 200; // 100
			
			// adc pedal torque angle adjusment
			if(ui16_adc_pedal_torque_delta < ADC_TORQUE_SENSOR_RANGE_TARGET_MAX) {
				ui16_temp = (ui16_adc_pedal_torque_delta * ADC_TORQUE_SENSOR_RANGE_TARGET_MAX)
					/ (ADC_TORQUE_SENSOR_RANGE_TARGET_MAX - (((ADC_TORQUE_SENSOR_RANGE_TARGET_MAX - ui16_adc_pedal_torque_delta) * 10)
					/ PEDAL_TORQUE_ADC_ANGLE_ADJ));
				
				ui16_adc_pedal_torque_delta = ui16_temp;
			}
		}
		else {
			ui16_adc_pedal_torque_delta = ui16_adc_pedal_torque - ui16_adc_pedal_torque_offset;
		}
		ui16_adc_pedal_torque_delta = (ui16_adc_pedal_torque_delta + ui16_adc_pedal_torque_delta_temp) >> 1;
	}
	else {
		ui16_adc_pedal_torque_delta = 0;
    }
	ui16_adc_pedal_torque_delta_temp = ui16_adc_pedal_torque_delta;
	
	// for startup assist without pedaling in power assist mode
	ui16_adc_pedal_torque_delta_no_boost = ui16_adc_pedal_torque_delta;
	
    // calculate torque on pedals
    ui16_pedal_torque_x100 = ui16_adc_pedal_torque_delta * ui8_pedal_torque_per_10_bit_ADC_step_x100;
	
	// calculate human power x10
	if((ui8_torque_sensor_calibrated)&&(m_configuration_variables.ui8_torque_sensor_adv_enabled)) {
		ui16_human_power_x10 = (uint16_t)(((uint32_t)ui16_adc_pedal_torque_delta * ui8_pedal_torque_per_10_bit_ADC_step_calc_x100 * ui8_pedal_cadence_RPM) / 96);
	}
	else {
		ui16_human_power_x10 = (uint16_t)(((uint32_t)ui16_pedal_torque_x100 * ui8_pedal_cadence_RPM) / 96); // see note below
	}
	// human power filtered x 10 for display data
	ui16_human_power_filtered_x10 = filter(ui16_human_power_x10, ui16_human_power_filtered_x10, 8);
	
	// startup boost mode
	switch (ui8_startup_boost_at_zero) {
		case CADENCE:
			ui8_startup_boost_flag = 1;
			break;
		case SPEED:
			if(ui16_wheel_speed_x10 == 0) {
				ui8_startup_boost_flag = 1;
			}
			else if(ui8_pedal_cadence_RPM > 45) {
				ui8_startup_boost_flag = 0;
			}
			break;
	}
	// pedal torque delta + startup boost in power assist mode
	if((m_configuration_variables.ui8_startup_boost_enabled)&&(ui8_startup_boost_flag)
	  &&(m_configuration_variables.ui8_riding_mode == POWER_ASSIST_MODE)) {
		// calculate startup boost torque & new pedal torque delta
		uint32_t ui32_temp = ((uint32_t)(ui16_adc_pedal_torque_delta * ui16_startup_boost_factor_array[ui8_pedal_cadence_RPM])) / 100;
		ui16_adc_pedal_torque_delta += (uint16_t) ui32_temp;
	}
	
	/*------------------------------------------------------------------------

    NOTE: regarding the human power calculation
    
    (1) Formula: power = torque * rotations per second * 2 * pi
    (2) Formula: power = torque * rotations per minute * 2 * pi / 60
    (3) Formula: power = torque * rotations per minute * 0.1047
    (4) Formula: power = torque * 100 * rotations per minute * 0.001047
    (5) Formula: power = torque * 100 * rotations per minute / 955
    (6) Formula: power * 10  =  torque * 100 * rotations per minute / 96
    
	------------------------------------------------------------------------*/
}


struct_configuration_variables* get_configuration_variables(void)
{
    return &m_configuration_variables;
}


static void check_brakes(void)
{
    if (ui8_brake_state)
        ui8_brakes_engaged = 1;
    else
        ui8_brakes_engaged = 0;
}


static void check_system(void)
{
// E08 ERROR_SPEED_SENSOR
#define CHECK_SPEED_SENSOR_COUNTER_THRESHOLD          500 // 500 * 25ms = 12.5 seconds
#define MOTOR_ERPS_SPEED_THRESHOLD	                  180
	static uint16_t ui16_check_speed_sensor_counter;
	static uint16_t ui16_error_speed_sensor_counter;
	
	// check speed sensor
	if((ui16_motor_speed_erps > MOTOR_ERPS_SPEED_THRESHOLD)
	  &&(m_configuration_variables.ui8_riding_mode != WALK_ASSIST_MODE)) {
		ui16_check_speed_sensor_counter++;
	}
	else {
		ui16_check_speed_sensor_counter = 0;
	}
	
	if(ui16_wheel_speed_x10) {
		ui16_check_speed_sensor_counter = 0;
	}
	
	if(ui16_check_speed_sensor_counter > CHECK_SPEED_SENSOR_COUNTER_THRESHOLD) {
		
		// set speed sensor error code
		ui8_system_state = ERROR_SPEED_SENSOR;
	}
	else if (ui8_system_state == ERROR_SPEED_SENSOR) {
		// increment speed sensor error reset counter
        ui16_error_speed_sensor_counter++;

        // check if the counter has counted to the set threshold for reset
        if (ui16_error_speed_sensor_counter > CHECK_SPEED_SENSOR_COUNTER_THRESHOLD) {
            // reset speed sensor error code
            if (ui8_system_state == ERROR_SPEED_SENSOR) {
				ui8_system_state = NO_ERROR;
				ui16_error_speed_sensor_counter = 0;
            }
		}
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// E03 ERROR_CADENCE_SENSOR
#define CHECK_CADENCE_SENSOR_COUNTER_THRESHOLD          1000 // 1000 * 25ms = 25 seconds
#define CADENCE_SENSOR_RESET_COUNTER_THRESHOLD         	250  // 250 * 25ms = 6,25 seconds
#define ADC_TORQUE_SENSOR_DELTA_THRESHOLD				(uint16_t)((ADC_TORQUE_SENSOR_RANGE_TARGET >> 1) + 20)
	static uint16_t ui16_check_cadence_sensor_counter;
	static uint8_t ui8_error_cadence_sensor_counter;
	
	// check cadence sensor
	if((ui16_adc_pedal_torque_delta_no_boost > ADC_TORQUE_SENSOR_DELTA_THRESHOLD)&&
	  ((ui8_pedal_cadence_RPM > 130)||(!ui8_pedal_cadence_RPM))) {
		ui16_check_cadence_sensor_counter++;
	}
	else {
		ui16_check_cadence_sensor_counter = 0;
	}
	
	if(ui16_check_cadence_sensor_counter > CHECK_CADENCE_SENSOR_COUNTER_THRESHOLD) {
		// set cadence sensor error code
		ui8_system_state = ERROR_CADENCE_SENSOR;
	}
	else if (ui8_system_state == ERROR_CADENCE_SENSOR) {
		// increment cadence sensor error reset counter
        ui8_error_cadence_sensor_counter++;

        // check if the counter has counted to the set threshold for reset
        if (ui8_error_cadence_sensor_counter > CADENCE_SENSOR_RESET_COUNTER_THRESHOLD) {
            // reset cadence sensor error code
            if (ui8_system_state == ERROR_CADENCE_SENSOR) {
				ui8_system_state = NO_ERROR;
				ui8_error_cadence_sensor_counter = 0;
            }
		}
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// E02 ERROR_TORQUE_SENSOR
    // check torque sensor
	if ((m_configuration_variables.ui8_riding_mode == POWER_ASSIST_MODE)
	  ||(m_configuration_variables.ui8_riding_mode == TORQUE_ASSIST_MODE)
	  ||(m_configuration_variables.ui8_riding_mode == HYBRID_ASSIST_MODE)
	  ||(m_configuration_variables.ui8_riding_mode == eMTB_ASSIST_MODE)) {
		ui8_riding_torque_mode = 1;
    
		if ((ui16_adc_pedal_torque_offset > 300)
		  ||(ui16_adc_pedal_torque_offset < 10)
		  ||(ui16_adc_pedal_torque > 500)
		  ||(ui8_adc_pedal_torque_offset_error)) {
			// set torque sensor error code
			ui8_system_state = ERROR_TORQUE_SENSOR;
		} else if (ui8_system_state == ERROR_TORQUE_SENSOR) {
			// reset error code
			ui8_system_state = NO_ERROR;
		}
	}
	else {
		ui8_riding_torque_mode = 0;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// E04 ERROR_MOTOR_BLOCKED
//#define MOTOR_BLOCKED_COUNTER_THRESHOLD               8  // 8 * 25ms = 0.2 second
//#define MOTOR_BLOCKED_BATTERY_CURRENT_THRESHOLD_X10   30 // 30 = 3.0 amps
//#define MOTOR_BLOCKED_ERPS_THRESHOLD                  20 // 20 ERPS
#define MOTOR_BLOCKED_RESET_COUNTER_THRESHOLD         	250  // 250 * 25ms = 6.25 seconds

    static uint8_t ui8_motor_blocked_counter;
    static uint8_t ui8_motor_blocked_reset_counter;

    // if the motor blocked error is enabled start resetting it
    if (ui8_system_state == ERROR_MOTOR_BLOCKED) {
        // increment motor blocked reset counter with 25 milliseconds
        ui8_motor_blocked_reset_counter++;

        // check if the counter has counted to the set threshold for reset
        if (ui8_motor_blocked_reset_counter > MOTOR_BLOCKED_RESET_COUNTER_THRESHOLD) {
            // reset motor blocked error code
            if (ui8_system_state == ERROR_MOTOR_BLOCKED) {
                ui8_system_state = NO_ERROR;
            }

            // reset the counter that clears the motor blocked error
            ui8_motor_blocked_reset_counter = 0;
        }
    } else {
        // if battery current is over the current threshold and the motor ERPS is below threshold start setting motor blocked error code
        if ((ui8_battery_current_filtered_x10 > MOTOR_BLOCKED_BATTERY_CURRENT_THRESHOLD_X10)
                && (ui16_motor_speed_erps < MOTOR_BLOCKED_ERPS_THRESHOLD)) {
            // increment motor blocked counter with 100 milliseconds
            ++ui8_motor_blocked_counter;

            // check if motor is blocked for more than some safe threshold
            if (ui8_motor_blocked_counter > ui8_motor_blocked_counter_threshold) {
                // set error code
                ui8_system_state = ERROR_MOTOR_BLOCKED;

                // reset motor blocked counter as the error code is set
                ui8_motor_blocked_counter = 0;
            }
        } else {
            // current is below the threshold and/or motor ERPS is above the threshold so reset the counter
            ui8_motor_blocked_counter = 0;
        }
    }
}


void ebike_control_lights(void)
{
#define DEFAULT_FLASH_ON_COUNTER_MAX      3
#define DEFAULT_FLASH_OFF_COUNTER_MAX     2
#define BRAKING_FLASH_ON_COUNTER_MAX      1
#define BRAKING_FLASH_OFF_COUNTER_MAX     1

    static uint8_t ui8_default_flash_state;
    static uint8_t ui8_default_flash_state_counter; // increments every function call -> 100 ms
    static uint8_t ui8_braking_flash_state;
    static uint8_t ui8_braking_flash_state_counter; // increments every function call -> 100 ms

    /****************************************************************************/

    // increment flash counters
    ++ui8_default_flash_state_counter;
    ++ui8_braking_flash_state_counter;

    /****************************************************************************/

    // set default flash state
    if ((ui8_default_flash_state) && (ui8_default_flash_state_counter > DEFAULT_FLASH_ON_COUNTER_MAX)) {
        // reset flash state counter
        ui8_default_flash_state_counter = 0;

        // toggle flash state
        ui8_default_flash_state = 0;
    } else if ((!ui8_default_flash_state) && (ui8_default_flash_state_counter > DEFAULT_FLASH_OFF_COUNTER_MAX)) {
        // reset flash state counter
        ui8_default_flash_state_counter = 0;

        // toggle flash state
        ui8_default_flash_state = 1;
    }

    /****************************************************************************/

    // set braking flash state
    if ((ui8_braking_flash_state) && (ui8_braking_flash_state_counter > BRAKING_FLASH_ON_COUNTER_MAX)) {
        // reset flash state counter
        ui8_braking_flash_state_counter = 0;

        // toggle flash state
        ui8_braking_flash_state = 0;
    } else if ((!ui8_braking_flash_state) && (ui8_braking_flash_state_counter > BRAKING_FLASH_OFF_COUNTER_MAX)) {
        // reset flash state counter
        ui8_braking_flash_state_counter = 0;

        // toggle flash state
        ui8_braking_flash_state = 1;
    }

    /****************************************************************************/

    // select lights configuration
    switch (m_configuration_variables.ui8_lights_configuration) {
    case 0:

        // set lights
        lights_set_state(ui8_lights_state);

        break;

    case 1:

        // check lights state
        if (ui8_lights_state) {
            // set lights
            lights_set_state(ui8_default_flash_state);
        } else {
            // set lights
            lights_set_state(ui8_lights_state);
        }

        break;

    case 2:

        // check light and brake state
        if (ui8_lights_state && ui8_brakes_engaged) {
            // set lights
            lights_set_state(ui8_braking_flash_state);
        } else {
            // set lights
            lights_set_state(ui8_lights_state);
        }

        break;

    case 3:

        // check light and brake state
        if (ui8_lights_state && ui8_brakes_engaged) {
            // set lights
            lights_set_state(ui8_brakes_engaged);
        } else if (ui8_lights_state) {
            // set lights
            lights_set_state(ui8_default_flash_state);
        } else {
            // set lights
            lights_set_state(ui8_lights_state);
        }

        break;

    case 4:

        // check light and brake state
        if (ui8_lights_state && ui8_brakes_engaged) {
            // set lights
            lights_set_state(ui8_braking_flash_state);
        } else if (ui8_lights_state) {
            // set lights
            lights_set_state(ui8_default_flash_state);
        } else {
            // set lights
            lights_set_state(ui8_lights_state);
        }

        break;

    case 5:

        // check brake state
        if (ui8_brakes_engaged) {
            // set lights
            lights_set_state(ui8_brakes_engaged);
        } else {
            // set lights
            lights_set_state(ui8_lights_state);
        }

        break;

    case 6:

        // check brake state
        if (ui8_brakes_engaged) {
            // set lights
            lights_set_state(ui8_braking_flash_state);
        } else {
            // set lights
            lights_set_state(ui8_lights_state);
        }

        break;

    case 7:

        // check brake state
        if (ui8_brakes_engaged) {
            // set lights
            lights_set_state(ui8_brakes_engaged);
        } else if (ui8_lights_state) {
            // set lights
            lights_set_state(ui8_default_flash_state);
        } else {
            // set lights
            lights_set_state(ui8_lights_state);
        }

        break;

    case 8:

        // check brake state
        if (ui8_brakes_engaged) {
            // set lights
            lights_set_state(ui8_braking_flash_state);
        } else if (ui8_lights_state) {
            // set lights
            lights_set_state(ui8_default_flash_state);
        } else {
            // set lights
            lights_set_state(ui8_lights_state);
        }

        break;

    default:

        // set lights
        lights_set_state(ui8_lights_state);

        break;
    }

    /*------------------------------------------------------------------------------------------------------------------

     NOTE: regarding the various light modes

     (0) lights ON when enabled
     (1) lights FLASHING when enabled

     (2) lights ON when enabled and BRAKE-FLASHING when braking
     (3) lights FLASHING when enabled and ON when braking
     (4) lights FLASHING when enabled and BRAKE-FLASHING when braking

     (5) lights ON when enabled, but ON when braking regardless if lights are enabled
     (6) lights ON when enabled, but BRAKE-FLASHING when braking regardless if lights are enabled

     (7) lights FLASHING when enabled, but ON when braking regardless if lights are enabled
     (8) lights FLASHING when enabled, but BRAKE-FLASHING when braking regardless if lights are enabled

     ------------------------------------------------------------------------------------------------------------------*/
	
	// display blinking code function
	ui8_display_blinking_code = ui8_default_flash_state;
}

// This is the interrupt that happens when UART2 receives data. We need it to be the fastest possible and so
// we do: receive every byte and assembly as a package, finally, signal that we have a package to process (on main slow loop)
// and disable the interrupt. The interrupt should be enable again on main loop, after the package being processed
void UART2_IRQHandler(void) __interrupt(UART2_IRQHANDLER)
{
	// Interrupt is received when Data is received or when overrun occured because UART2_IT_RXNE_OR interrupt is used
	// test error flags : OverRun, Noise and Framing error flags
    if ((UART2->SR & (uint8_t)(UART2_FLAG_OR_LHE|UART2_FLAG_NF|UART2_FLAG_FE)) != (uint8_t)0x00)
	{
	    // There is an error
		// Clear error bits
		ui8_byte_received = (uint8_t)UART2->DR; // According to datasheet : The Error bits (OR, NF, FE) are reset by a read to the UART_SR register followed by a UART_DR
		// Trash all received data and wait for next message
	 	ui8_rx_counter = 0;
		ui8_state_machine = 0;
	}
	else if((UART2->SR & (uint8_t)(UART2_FLAG_RXNE)) != (uint8_t)0x00)
	{
		// Read data : Reading data clear UART2_FLAG_RXNE flag
		ui8_byte_received = (uint8_t)UART2->DR;

		switch(ui8_state_machine)
		{
			case 0:
				if(ui8_byte_received == RX_STX) // see if we get start package byte
				{
					ui8_rx_buffer[ui8_rx_counter] = ui8_byte_received;
					ui8_rx_counter++;
					ui8_state_machine = 1;
				}
				else
				{
					ui8_rx_counter = 0;
					ui8_state_machine = 0;
				}
			break;

			case 1:
				ui8_rx_buffer[ui8_rx_counter] = ui8_byte_received;
				ui8_rx_counter++;

				// see if is the last byte of the package
				if(ui8_rx_counter >= UART_RX_BUFFER_LEN)
				{
				ui8_rx_counter = 0;
				ui8_state_machine = 0;
				ui8_received_package_flag = 1; // signal that we have a full package to be processed
				UART2->CR2 &= ~(1 << 5); // disable UART2 receive interrupt
				}			
			break;

			default:
			break;
	
		}
	}
}

static uint8_t no_rx_counter = 0;

static void uart_receive_package(void)
{
	uint8_t ui8_i;
	uint8_t ui8_rx_check_code;
	uint8_t ui8_assist_level_mask;
	
	// increment the comms safety counter
    no_rx_counter++;
	
	// increment walk assist counter
	ui8_walk_assist_counter++;
	
	// increment startup assist counter
	ui8_startup_assist_counter++;
	
	// increment cruise counter
	ui8_cruise_counter++;
		
	// increment lights_counter
	ui8_lights_counter++;
	
	// increment display menu counter
	ui8_menu_counter++;

	if(ui8_received_package_flag)
	{
		// verify check code of the package
		ui8_rx_check_code = 0x00;
							 
		for(ui8_i = 0; ui8_i < RX_CHECK_CODE; ui8_i++)
		{
			ui8_rx_check_code += ui8_rx_buffer[ui8_i];
		}

		// see if check code is ok...
		if(ui8_rx_check_code == ui8_rx_buffer[RX_CHECK_CODE])
		{
			// Reset the safety counter when a valid message from the LCD is received
            no_rx_counter = 0;
			
			// mask assist level from display
			//ui8_assist_level_mask = ui8_rx_buffer[1] & 0x5E; // mask: 01011110
			ui8_assist_level_mask = ui8_rx_buffer[1] & 0xDE; // mask: 11011110
			ui8_assist_level_01_flag = 0;
			
			// set assist level
			switch(ui8_assist_level_mask)
			{
				case ASSIST_PEDAL_LEVEL0: ui8_assist_level = OFF; break;
				case ASSIST_PEDAL_LEVEL01:
					ui8_assist_level = ECO;
					ui8_assist_level_01_flag = 1;
					break;
				case ASSIST_PEDAL_LEVEL1: ui8_assist_level = ECO; break;
				case ASSIST_PEDAL_LEVEL2: ui8_assist_level = TOUR; break;
				case ASSIST_PEDAL_LEVEL3: ui8_assist_level = SPORT; break;
				case ASSIST_PEDAL_LEVEL4: ui8_assist_level = TURBO; break;
			}
			
			if(!ui8_display_ready_flag)
				// assist level temp at power on
				ui8_assist_level_temp = ui8_assist_level;
			
			// display ready
			ui8_display_ready_flag = 1;
			
			// display lights button pressed:
			if(ui8_rx_buffer[1] & 0x01)
			{
				// lights off:
				if((!ui8_lights_flag)&&
				  ((m_configuration_variables.ui8_set_parameter_enabled)||
				  (ui8_assist_level == OFF)||
				  ((ui8_startup_counter < DELAY_MENU_ON)&&(ui8_assist_level == TURBO))))
				{
					// lights 5s on
					ui8_lights_on_5s = 1;
					
					// menu flag
					if(!ui8_menu_flag)
					{
						// set menu flag
						ui8_menu_flag = 1;
							
						// set menu index
						if(++ui8_menu_index > 3) {ui8_menu_index = 3;}
						
						// display status alternative lights configuration
						ui8_display_alternative_lights_configuration = 0;
						
						// restore previous parameter
						switch(ui8_assist_level)
						{	
							case OFF:
								switch(ui8_menu_index)
								{
									case 2:
										// restore previous set parameter
										m_configuration_variables.ui8_set_parameter_enabled = ui8_set_parameter_enabled_temp;
										ui8_display_function_status[0][OFF] = m_configuration_variables.ui8_set_parameter_enabled;
										break;
									case 3:
										// restore previous auto display data
										m_configuration_variables.ui8_auto_display_data_enabled = ui8_auto_display_data_enabled_temp;
										ui8_display_function_status[1][OFF] = m_configuration_variables.ui8_auto_display_data_enabled;
										break;
								}
								break;
								
							case ECO:
								switch(ui8_menu_index)
								{
									case 2:
										// restore previous street mode
										m_configuration_variables.ui8_street_mode_enabled = ui8_street_mode_enabled_temp;
										ui8_display_function_status[0][ECO] = m_configuration_variables.ui8_street_mode_enabled;
										break;
									case 3:
										// restore previous startup boost
										m_configuration_variables.ui8_startup_boost_enabled = ui8_startup_boost_enabled_temp;
										ui8_display_function_status[1][ECO] = m_configuration_variables.ui8_startup_boost_enabled;
									
										if(ui8_display_torque_sensor_flag_2)
										{
											// set display torque sensor step for calibration
											ui8_display_torque_sensor_step = 1;
											// delay display torque sensor step for calibration
											ui8_delay_display_function = DELAY_DISPLAY_TORQUE_CALIBRATION;
										}
										else if(ui8_display_torque_sensor_flag_1)
										{
											// restore torque sensor advanced
											m_configuration_variables.ui8_torque_sensor_adv_enabled = ui8_torque_sensor_adv_enabled_temp;
											ui8_display_function_status[2][ECO] = m_configuration_variables.ui8_torque_sensor_adv_enabled;
											
											// set display torque sensor value for calibration
											ui8_display_torque_sensor_value = 1;
											// delay display torque sensor value for calibration
											ui8_delay_display_function = DELAY_DISPLAY_TORQUE_CALIBRATION;
										}
										break;
								}
								break;
							
							case TURBO:	
								switch(ui8_menu_index)
								{
									case 2:
										if(ui8_lights_configuration_2 == 9)
										{
											// restore previous lights configuration
											m_configuration_variables.ui8_lights_configuration = ui8_lights_configuration_temp;
											ui8_display_lights_configuration = m_configuration_variables.ui8_lights_configuration;
											// display status
											ui8_display_alternative_lights_configuration = 1;
										}
										break;
									case 3:
										if(ui8_lights_configuration_2 == 9)
										{
											// restore previous assist without pedal rotation
											m_configuration_variables.ui8_assist_without_pedal_rotation_enabled = ui8_assist_without_pedal_rotation_temp;
											ui8_display_function_status[1][TURBO] = m_configuration_variables.ui8_assist_without_pedal_rotation_enabled;
										}
										else
										{
											// restore previous lights configuration
											m_configuration_variables.ui8_lights_configuration = ui8_lights_configuration_temp;
											ui8_display_lights_configuration = m_configuration_variables.ui8_lights_configuration;
										}
										
										if(ui8_lights_configuration_3 == 10)
										{
											// display status
											ui8_display_alternative_lights_configuration = 1;
										}
										break;
								}							
								break;
						}
						
						// display function code enabled (E02, E03,E04)
						ui8_display_function_code = ui8_menu_index + 1;
						// display function code temp (for display function status VLCD5/6)
						ui8_display_function_code_temp = ui8_display_function_code;
						// display data function enabled
						ui8_display_data_enabled = 1;
						
						// restart lights counter
						ui8_lights_counter = 0;	
						// restart menu counter
						ui8_menu_counter = 0;					
					}
					
					// after some seconds: switch on lights (if enabled) and abort function
					if((ui8_lights_counter >= DELAY_LIGHTS_ON)||
					  ((ui8_assist_level != ui8_assist_level_temp)&&(!ui8_torque_sensor_calibration_flag)&&(!ui8_auto_display_data_flag)))
					{
						// set lights flag
						ui8_lights_flag = 1;
						// lights 5s off		
						ui8_lights_on_5s = 0;
						// clear menu flag
						ui8_menu_flag = 0;
						// clear menu index
						ui8_menu_index = 0;
						// clear menu counter
						ui8_menu_counter = ui8_delay_display_function;
						// clear lights counter
						ui8_lights_counter = DELAY_LIGHTS_ON;
						// display function code disabled
						ui8_display_function_code = NO_FUNCTION;
					}
				}
				else
				{
					// set lights flag
					ui8_lights_flag = 1;
				}
			
			}
			else
			{
				// lights off:
				if(!ui8_lights_flag)
				{
					// menu flag active:
					if(ui8_menu_flag)
					{
						// clear menu flag
						ui8_menu_flag = 0;
							
						// lights 5s off		
						ui8_lights_on_5s = 0;	
							
						// restart menu counter
						ui8_menu_counter = 0;
								
						// menu function enabled
						ui8_menu_function_enabled = 1;
					}
				}
				// lights on:
				else
				{
					// clear lights flag
					ui8_lights_flag = 0;
				}
			}

			// restart menu display function
			if((ui8_menu_counter >= ui8_delay_display_function)||
			  ((ui8_assist_level != ui8_assist_level_temp)&&(!ui8_torque_sensor_calibration_flag)&&(!ui8_auto_display_data_flag)))
			{					
				// clear menu flag
				ui8_menu_flag = 0;
				// clear menu index
				ui8_menu_index = 0;
				// clear menu counter
				ui8_menu_counter = ui8_delay_display_function;
				// menu function disabled
				ui8_menu_function_enabled = 0;
				// display data function disabled
				ui8_display_data_enabled = 0;
				// display function code disabled
				ui8_display_function_code = NO_FUNCTION;
				// display torque flag 1 disabled
				ui8_display_torque_sensor_flag_1 = 0;
				// display torque flag 2 disabled
				ui8_display_torque_sensor_flag_2 = 0;
				// display torque value disabled
				ui8_display_torque_sensor_value = 0;
				// display torque step disabled
				ui8_display_torque_sensor_step = 0;
				// clear display soc %
				ui8_display_battery_soc = 0;
			}

			// display menu function
			if(ui8_menu_function_enabled)
			{
				// display status lights configuration
				ui8_display_alternative_lights_configuration = 0;
				// set display parameter
				if((m_configuration_variables.ui8_set_parameter_enabled)||
				  (ui8_assist_level == OFF)||
				  ((ui8_startup_counter < DELAY_MENU_ON)&&(ui8_assist_level == TURBO)))
				{
					switch(ui8_assist_level)
					{	
						case OFF:
							// set parameter
							switch(ui8_menu_index)
							{
								case 1:
									// for restore set parameter
									ui8_set_parameter_enabled_temp = m_configuration_variables.ui8_set_parameter_enabled;
									
									// set parameter enabled
									m_configuration_variables.ui8_set_parameter_enabled = !m_configuration_variables.ui8_set_parameter_enabled;
									ui8_display_function_status[0][OFF] = m_configuration_variables.ui8_set_parameter_enabled;
									break;
								case 2:		   
									// for restore auto display data
									ui8_auto_display_data_enabled_temp = m_configuration_variables.ui8_auto_display_data_enabled;
									
									// set auto display data
									m_configuration_variables.ui8_auto_display_data_enabled = !m_configuration_variables.ui8_auto_display_data_enabled;
									ui8_display_function_status[1][OFF] = m_configuration_variables.ui8_auto_display_data_enabled;
									break;
								case 3:
									// save current configuration
									EEPROM_controller(WRITE_TO_MEMORY, EEPROM_BYTES_INIT_OEM_DISPLAY);
									break;
							}
							break;
						
						case ECO:
							// set street/offroad mode
							switch(ui8_menu_index)
							{
								case 1:
									// for restore street mode
									ui8_street_mode_enabled_temp = m_configuration_variables.ui8_street_mode_enabled;
									
									// change street mode
									m_configuration_variables.ui8_street_mode_enabled = !m_configuration_variables.ui8_street_mode_enabled;
									ui8_display_function_status[0][ECO] = m_configuration_variables.ui8_street_mode_enabled;
									break;
								case 2:																		 
									// for restore startup boost
									ui8_startup_boost_enabled_temp = m_configuration_variables.ui8_startup_boost_enabled;	
									
									// change startup boost
									m_configuration_variables.ui8_startup_boost_enabled = !m_configuration_variables.ui8_startup_boost_enabled;
									ui8_display_function_status[1][ECO] = m_configuration_variables.ui8_startup_boost_enabled;
									break;
								case 3:
									if(!ui8_display_torque_sensor_value)
									{
										// for restore torque sensor advanced
										ui8_torque_sensor_adv_enabled_temp = m_configuration_variables.ui8_torque_sensor_adv_enabled;
									
										// change torque sensor advanced mode
										m_configuration_variables.ui8_torque_sensor_adv_enabled = !m_configuration_variables.ui8_torque_sensor_adv_enabled;
										ui8_display_function_status[2][ECO] = m_configuration_variables.ui8_torque_sensor_adv_enabled;
										
										// set display torque sensor flag 1 for value calibration
										ui8_display_torque_sensor_flag_1 = 1;
									}									
									else
									{
										// for recovery actual riding mode
										if(m_configuration_variables.ui8_riding_mode != TORQUE_SENSOR_CALIBRATION_MODE)
											ui8_riding_mode_temp = m_configuration_variables.ui8_riding_mode;
										// special riding mode (torque sensor calibration)
										m_configuration_variables.ui8_riding_mode = TORQUE_SENSOR_CALIBRATION_MODE;
										
										// set display torque sensor flag 2 for step calibration
										ui8_display_torque_sensor_flag_2 = 1;
									}
									break;
							}
							break;

						case TOUR:
							// set riding mode 1
							switch(ui8_menu_index)
							{
								case 1: m_configuration_variables.ui8_riding_mode = POWER_ASSIST_MODE; break;
								case 2: m_configuration_variables.ui8_riding_mode = TORQUE_ASSIST_MODE; break;
								case 3: m_configuration_variables.ui8_riding_mode = CADENCE_ASSIST_MODE; break;
							}
							ui8_display_riding_mode = m_configuration_variables.ui8_riding_mode;
							break;
						
						case SPORT:
							// set riding mode 2
							switch(ui8_menu_index)
							{
								case 1:	m_configuration_variables.ui8_riding_mode = eMTB_ASSIST_MODE; break;
								case 2: m_configuration_variables.ui8_riding_mode = HYBRID_ASSIST_MODE; break;          
								case 3: m_configuration_variables.ui8_riding_mode = CRUISE_MODE; break;
							}
							ui8_display_riding_mode = m_configuration_variables.ui8_riding_mode;
							break;
						
						case TURBO:
							// set lights mode
							switch(ui8_menu_index)
							{
								case 1:  
									if(ui8_startup_counter < DELAY_MENU_ON)
									{
										// manually setting battery percentage x10 (actual charge) within 5 seconds of power on
										ui16_battery_SOC_percentage_x10 = read_battery_soc();
										// calculate watt-hours x10
										ui32_wh_x10_offset = ((uint32_t) (1000 - ui16_battery_SOC_percentage_x10) * ui16_actual_battery_capacity) / 100;
										// for display soc %
										ui8_display_battery_soc = 1;
									}
									else
									{
										// for restore lights configuration
										ui8_lights_configuration_temp = m_configuration_variables.ui8_lights_configuration;
									
										if(m_configuration_variables.ui8_lights_configuration != LIGHTS_CONFIGURATION_ON_STARTUP)
											m_configuration_variables.ui8_lights_configuration = LIGHTS_CONFIGURATION_ON_STARTUP;
										else
											m_configuration_variables.ui8_lights_configuration = LIGHTS_CONFIGURATION_1;
									}
									break;
								case 2:
									if(ui8_lights_configuration_2 == 9)
									{
										// for restore assist without pedal rotation
										ui8_assist_without_pedal_rotation_temp = m_configuration_variables.ui8_assist_without_pedal_rotation_enabled;
									
										// change assist without pedal rotation
										m_configuration_variables.ui8_assist_without_pedal_rotation_enabled = !m_configuration_variables.ui8_assist_without_pedal_rotation_enabled;
										ui8_display_function_status[1][TURBO] = m_configuration_variables.ui8_assist_without_pedal_rotation_enabled;
										// display status
										ui8_display_alternative_lights_configuration = 1;
									}
									else
									{
										m_configuration_variables.ui8_lights_configuration = LIGHTS_CONFIGURATION_2;
										// for restore lights configuration
										ui8_lights_configuration_temp = m_configuration_variables.ui8_lights_configuration;
									
									}
									break;
								case 3:
									if(ui8_lights_configuration_3 == 10)
									{
										// change system error enabled
										m_configuration_variables.ui8_assist_with_error_enabled = !m_configuration_variables.ui8_assist_with_error_enabled;
										ui8_display_function_status[2][TURBO] = m_configuration_variables.ui8_assist_with_error_enabled;
										// display status
										ui8_display_alternative_lights_configuration = 1;
									}
									else
									{
										m_configuration_variables.ui8_lights_configuration = LIGHTS_CONFIGURATION_3;
									}
									break;
							}
							// display lights configuration
							ui8_display_lights_configuration = m_configuration_variables.ui8_lights_configuration;
							break;
					}
					
					// delay display menu function 
					if(!ui8_display_torque_sensor_value)
						ui8_delay_display_function = DELAY_MENU_ON;

					// display data value enabled
					ui8_display_data_enabled = 1;
				}
			}
			
			// display function status VLCD5/6
			#if ENABLE_VLCD5 || ENABLE_VLCD6
			if(ui8_menu_flag)
			{
				if(ui8_menu_counter >= DELAY_FUNCTION_STATUS)
					// display function code disabled
					ui8_display_function_code = NO_FUNCTION;
			}
			else
			{
				if((ui8_menu_counter > (DELAY_MENU_ON - DELAY_FUNCTION_STATUS))&&
					(ui8_menu_counter < DELAY_MENU_ON)&&(ui8_menu_index))
					// restore display function code
					ui8_display_function_code = ui8_display_function_code_temp;
				else
					// display function code disabled
					ui8_display_function_code = NO_FUNCTION;
			}
			#endif
			
			// menu function disabled
			ui8_menu_function_enabled = 0;

			// special riding modes with walk assist button
			switch(m_configuration_variables.ui8_riding_mode)
			{	
				case TORQUE_SENSOR_CALIBRATION_MODE:
					#if ENABLE_XH18 || ENABLE_VLCD5 || ENABLE_850C
					if (((ui8_assist_level != OFF)&&(ui8_assist_level != ECO))||(ui8_menu_counter >= ui8_delay_display_function))
					#else // ENABLE VLCD6
					if ((ui8_assist_level != ECO)||(ui8_menu_counter >= ui8_delay_display_function))
					#endif
					{	
						// riding mode recovery at level change
						m_configuration_variables.ui8_riding_mode = ui8_riding_mode_temp;
						// clear torque sensor calibration flag
						ui8_torque_sensor_calibration_flag = 0;
						// display data function disabled
						ui8_display_data_enabled = 0;
						// clear menu counter
						ui8_menu_counter = ui8_delay_display_function;
					}
					else
					{
						if(!ui8_torque_sensor_calibration_flag)
						{
							// set torque sensor calibration flag
							ui8_torque_sensor_calibration_flag = 1;
							// restart menu counter
							ui8_menu_counter = 0;
						}

						// walk assist button pressed
						if((ui8_rx_buffer[1] & 0x20)&&(ui8_display_ready_flag))
							ui8_torque_sensor_calibration_started = 1;
						else
							ui8_torque_sensor_calibration_started = 0;

						// display data function enabled
						//ui8_display_data_enabled = 1;
						
						#if ENABLE_VLCD5 || ENABLE_VLCD6
						// display function code disabled
						ui8_display_function_code = NO_FUNCTION;
						#endif
					}
					break;
					
				case CRUISE_MODE:
					// walk assist button pressed
					if((ui8_rx_buffer[1] & 0x20)&&(ui8_display_ready_flag))
						ui8_cruise_button_flag = 1;
					else
						ui8_cruise_button_flag = 0;
					break;
				
				default:
					// startup assist mode
					#if STARTUP_ASSIST_ENABLED
					// walk assist button pressed
					if((ui8_rx_buffer[1] & 0x20)&&(ui8_display_ready_flag)
						&&(!ui8_walk_assist_flag)&&(ui8_lights_flag))
						ui8_startup_assist = 1;
						// startup assist max time
						if(ui8_startup_assist_counter > STARTUP_ASSIST_MAX_TIME)
							ui8_startup_assist = 0;
					else
						ui8_startup_assist = 0;
						// restart startup assist counter
						ui8_startup_assist_counter = 0;
					#endif
					
					// walk assist mode
					#if ENABLE_WALK_ASSIST
					// walk assist button pressed
					if((ui8_rx_buffer[1] & 0x20)&&(ui8_display_ready_flag)&&(!ui8_startup_assist)
						&&((!m_configuration_variables.ui8_street_mode_enabled)||(ui8_street_mode_walk_assist_enabled)))
					{
						if(!ui8_walk_assist_flag)
						{
							// set walk assist flag
							ui8_walk_assist_flag = 1;
							// for restore riding mode
							ui8_riding_mode_temp = m_configuration_variables.ui8_riding_mode;
							// walk assist level
							ui8_walk_assist_level = ui8_assist_level;
							// set walk assist mode
							m_configuration_variables.ui8_riding_mode = WALK_ASSIST_MODE;
						}
				
						#if WALK_ASSIST_DEBOUNCE_ENABLED && ENABLE_BRAKE_SENSOR
						// restart walk assist counter
						ui8_walk_assist_counter = 0;
						#endif
					}
					else
					{
						#if WALK_ASSIST_DEBOUNCE_ENABLED && ENABLE_BRAKE_SENSOR
						if((ui8_walk_assist_counter < WALK_ASSIST_DEBOUNCE_TIME) &&
						  (ui16_wheel_speed_x10 < WALK_ASSIST_THRESHOLD_SPEED_X10))
						{
							// stop walk assist during debounce time
							#if ENABLE_XH18 || ENABLE_VLCD5
							// XH18 level up button
							if((ui8_assist_level > ui8_walk_assist_level)||(ui8_brakes_engaged))
							#else
							// VLCD6 or 850C level up or down button
							if((ui8_assist_level != ui8_walk_assist_level)||(ui8_brakes_engaged))
							#endif
							{
								if(ui8_walk_assist_flag)
									// restore previous riding mode
									m_configuration_variables.ui8_riding_mode = ui8_riding_mode_temp;
								
								// clear walk assist flag
								ui8_walk_assist_flag = 0;
							}
						}
						else
						{
							// restore previous riding mode
							if(ui8_walk_assist_flag)
								m_configuration_variables.ui8_riding_mode = ui8_riding_mode_temp;
					
							// clear walk assist flag
							ui8_walk_assist_flag = 0;
							// reset walk assist speed flag
							ui8_walk_assist_speed_flag = 0;
						}
						#else
						// restore previous riding mode
						if(ui8_walk_assist_flag)
							m_configuration_variables.ui8_riding_mode = ui8_riding_mode_temp;
					
						// clear walk assist flag
						ui8_walk_assist_flag = 0;
						// reset walk assist speed flag
						ui8_walk_assist_speed_flag = 0;
						#endif
					}
					#endif
					break;
			}
			
			// set assist parameter
			ui8_riding_mode_parameter = ui8_riding_mode_parameter_array[m_configuration_variables.ui8_riding_mode - 1][ui8_assist_level];
			if(ui8_assist_level_01_flag)
				ui8_riding_mode_parameter = (uint8_t)(((uint16_t) ui8_riding_mode_parameter * ASSIST_PEDAL_LEVEL01_PERCENT) / 100);
			
			// automatic data display at lights on
			if(m_configuration_variables.ui8_auto_display_data_enabled)
			{	
				if((ui8_lights_flag)&&(!ui8_menu_index)&&(!ui8_display_battery_soc)&&(!ui8_display_torque_sensor_value)&&(!ui8_display_torque_sensor_step))
				{
					if(!ui8_auto_display_data_flag)
					{	
						// set auto display data flag
						ui8_auto_display_data_flag = 1;
						ui8_auto_display_data_status = 1;
						// display data function enabled
						ui8_display_data_enabled = 1;
						// restart menu counter
						ui8_menu_counter = 0;
						// set data index
						ui8_data_index = 0;
						// assist level temp, ignore first change
						ui8_assist_level_temp = ui8_assist_level;
						// delay data function
						if(ui8_delay_display_array[ui8_data_index])
							ui8_delay_display_function  = ui8_delay_display_array[ui8_data_index];
						else
							ui8_delay_display_function  = DELAY_MENU_ON;
					}
					
					// restart menu counter if data delay is zero
					if(!ui8_delay_display_array[ui8_data_index])
						ui8_menu_counter = 0;
						
					if((ui8_data_index + 1) < AUTO_DATA_NUMBER_DISPLAY)
					{
						if((ui8_menu_counter >= (ui8_delay_display_function - 4))||(ui8_assist_level != ui8_assist_level_temp))
						{
							// restart menu counter
							ui8_menu_counter = 0;
							// increment data index
							ui8_data_index++;
							// delay data function
							if(ui8_delay_display_array[ui8_data_index])
								ui8_delay_display_function  = ui8_delay_display_array[ui8_data_index];
							else
								ui8_delay_display_function  = DELAY_MENU_ON;
						}
					}
					else if(ui8_menu_counter >= ui8_delay_display_function)
					{
						ui8_auto_display_data_status = 0;
					}
				}
				else
				{
					if(ui8_auto_display_data_flag)
					{
						// reset auto display data flag
						ui8_auto_display_data_flag = 0;
						ui8_auto_display_data_status = 0;
						// display data function disabled
						ui8_display_data_enabled = 0;
					}
				}
			}
			
			// assist level temp, to change or stop operation at change of level
			ui8_assist_level_temp = ui8_assist_level;
			
			// set lights
			#if ENABLE_LIGHTS
			// switch on/switch off lights
			if((ui8_lights_flag)||(ui8_lights_on_5s))
				ui8_lights_state = 1;
			else
				ui8_lights_state = 0;
			#endif
			
			// get wheel diameter from display
			ui8_oem_wheel_diameter = ui8_rx_buffer[3];
			
			// factor to calculate the value of the data to be displayed
			ui16_display_data_factor = OEM_WHEEL_FACTOR * ui8_oem_wheel_diameter;
			
			// get wheel max speed from display
			ui16_oem_wheel_speed_max = ui8_rx_buffer[5];
					
			// street limit
			if(m_configuration_variables.ui8_street_mode_enabled)
			{	
				#if STREET_MODE_POWER_LIMIT_ENABLED
				// battery power street limit
				ui8_target_battery_max_power_div25 = STREET_MODE_POWER_LIMIT_DIV25;
				#else
				// battery max power
				ui8_target_battery_max_power_div25 = TARGET_MAX_BATTERY_POWER_DIV25;
				#endif
				
				#if ENABLE_WHEEL_MAX_SPEED_FROM_DISPLAY	
				if(ui16_oem_wheel_speed_max > STREET_MODE_SPEED_LIMIT)
				{
					// wheel speed street limit
					m_configuration_variables.ui8_wheel_speed_max = STREET_MODE_SPEED_LIMIT;
				}
				else
				{
					// wheel max speed from display
					m_configuration_variables.ui8_wheel_speed_max = ui16_oem_wheel_speed_max;
				}
				#else
				// wheel speed street limit
				m_configuration_variables.ui8_wheel_speed_max = STREET_MODE_SPEED_LIMIT;
				#endif
				
				// street cruise mode
				#if STREET_MODE_CRUISE_ENABLED
				// cruise mode speed limit
				if((m_configuration_variables.ui8_riding_mode == CRUISE_MODE)&&(ui8_riding_mode_parameter > STREET_MODE_SPEED_LIMIT))
				{	
					ui8_riding_mode_parameter = STREET_MODE_SPEED_LIMIT;;
				}
				#endif
				
				// disable throttle
				#if !STREET_MODE_THROTTLE_ENABLED
				if(ui8_optional_ADC_function == THROTTLE_CONTROL)
				{
					ui8_optional_ADC_function = NOT_IN_USE;
				}
				#endif
			}
			else
			{	
				// battery power limit
				ui8_target_battery_max_power_div25 = TARGET_MAX_BATTERY_POWER_DIV25;
				
				#if ENABLE_WHEEL_MAX_SPEED_FROM_DISPLAY
				// wheel max speed from display
				m_configuration_variables.ui8_wheel_speed_max = ui16_oem_wheel_speed_max;
				#else
				// wheel max speed
				m_configuration_variables.ui8_wheel_speed_max = WHEEL_MAX_SPEED;
				#endif
				
				// enable throttle
				if(ui8_optional_ADC_function_temp == THROTTLE_CONTROL)
					ui8_optional_ADC_function = THROTTLE_CONTROL;
			}
			
			// calculate max battery current in ADC steps from the received battery current limit
			uint8_t ui8_adc_battery_current_max_temp_1 = (uint16_t)(m_configuration_variables.ui8_battery_current_max * (uint8_t)100)
                        / (uint16_t)BATTERY_CURRENT_PER_10_BIT_ADC_STEP_X100;
			// calculate max battery current in ADC steps from the received power limit
			uint32_t ui32_battery_current_max_x100 = ((uint32_t) ui8_target_battery_max_power_div25 * 2500000)
						/ ui16_battery_voltage_filtered_x1000;
			uint8_t ui8_adc_battery_current_max_temp_2 = ui32_battery_current_max_x100 / BATTERY_CURRENT_PER_10_BIT_ADC_STEP_X100;  
			// set max battery current
			ui8_adc_battery_current_max = ui8_min(ui8_adc_battery_current_max_temp_1, ui8_adc_battery_current_max_temp_2);
			// set max motor phase current
			uint16_t ui16_temp = ui8_adc_battery_current_max * ADC_10_BIT_MOTOR_PHASE_CURRENT_MAX;
			ui8_adc_motor_phase_current_max = (uint8_t)(ui16_temp / ADC_10_BIT_BATTERY_CURRENT_MAX);
			// limit max motor phase current if higher than configured hardware limit (safety)
			if (ui8_adc_motor_phase_current_max > ADC_10_BIT_MOTOR_PHASE_CURRENT_MAX) {
				ui8_adc_motor_phase_current_max = ADC_10_BIT_MOTOR_PHASE_CURRENT_MAX;
			}
			
			// set pedal torque per 10_bit DC_step x100 advanced
			if((ui8_torque_sensor_calibrated)&&(m_configuration_variables.ui8_torque_sensor_adv_enabled)) {
				ui8_pedal_torque_per_10_bit_ADC_step_x100 = PEDAL_TORQUE_PER_10_BIT_ADC_STEP_ADV_X100;
			}
			else {
				ui8_pedal_torque_per_10_bit_ADC_step_x100 = PEDAL_TORQUE_PER_10_BIT_ADC_STEP_X100;
			}
		}
		
		// signal that we processed the full package
		ui8_received_package_flag = 0;
		
		// assist level = OFF if connection with the LCD is lost for more than 0,6 sec (safety)
		if (no_rx_counter > 6)
			ui8_assist_level = OFF;
		
		// enable UART2 receive interrupt as we are now ready to receive a new package
		UART2->CR2 |= (1 << 5);
	}
}


static void uart_send_package(void)
{	
	uint8_t ui8_i;
	uint8_t ui8_tx_check_code;
	
	// display ready
	if(ui8_display_ready_flag)
	{
		// send the data to the LCD
		// start up byte
		ui8_tx_buffer[0] = TX_STX;

		// clear fault code
		ui8_display_fault_code = NO_FAULT;

		// initialize working status
		ui8_working_status &= 0xFE; // bit0 = 0 (battery normal)

		#if ENABLE_VLCD6 || ENABLE_XH18
		switch(ui8_battery_state_of_charge)
		{
			case 0:
				ui8_working_status |= 0x01; // bit0 = 1 (battery undervoltage)
				ui8_tx_buffer[1] = 0x00;
				break;
			case 1:
				ui8_tx_buffer[1] = 0x00; // Battery 0/4 (empty and blinking)
				break;
			case 2:
				ui8_tx_buffer[1] = 0x02; // Battery 1/4
				break;
			case 3:
				ui8_tx_buffer[1] = 0x06; // Battery 2/4
				break;
			case 4:
				ui8_tx_buffer[1] = 0x09; // Battery 3/4
				break;
			case 5:
				ui8_tx_buffer[1] = 0x0C; // Battery 4/4 (full)
				break;
			case 6:
				ui8_tx_buffer[1] = 0x0C; // Battery 4/4 (soc reset)
				break;
			case 7:
				ui8_tx_buffer[1] = 0x0C; // Battery 4/4 (full)
				// E01 (E06 blinking for XH18) ERROR_OVERVOLTAGE
				ui8_display_fault_code = ERROR_OVERVOLTAGE; // Fault overvoltage
				break;
		}
		#else // ENABLE_VLCD5 or 850C
		switch(ui8_battery_state_of_charge)
		{
			case 0:
				ui8_working_status |= 0x01; // bit0 = 1 (battery undervoltage)
				ui8_tx_buffer[1] = 0x00;
				break;
			case 1:
				ui8_tx_buffer[1] = 0x00; // Battery 0/6 (empty and blinking)
				break;
			case 2:
				ui8_tx_buffer[1] = 0x02; // Battery 1/6
				break;
			case 3:
				ui8_tx_buffer[1] = 0x04; // Battery 2/6
				break;
			case 4:
				ui8_tx_buffer[1] = 0x06; // Battery 3/6
				break;
			case 5:
				ui8_tx_buffer[1] = 0x08; // Battery 4/6
				break;
			case 6:
				ui8_tx_buffer[1] = 0x0A; // Battery 5/6
				break;
			case 7:
				ui8_tx_buffer[1] = 0x0C; // Battery 6/6 (full)
				break;
			case 8:
				ui8_tx_buffer[1] = 0x0C; // Battery 6/6 (soc reset)
				break;
			case 9:
				ui8_tx_buffer[1] = 0x0C; // Battery 6/6 (full)
				// E01 ERROR_OVERVOLTAGE
				ui8_display_fault_code = ERROR_OVERVOLTAGE; // Fault overvoltage
				break;
		}
		#endif

		// reserved for VLCD5, torque sensor value TE and TE1
		#if ENABLE_VLCD5
		ui8_tx_buffer[3] = ui16_adc_pedal_torque_offset_init;
		if (ui16_adc_pedal_torque > ui16_adc_pedal_torque_offset_init) {
			ui8_tx_buffer[4] = ui16_adc_pedal_torque - ui16_adc_pedal_torque_offset_init;
		}
		else {
			ui8_tx_buffer[4] = 0;
		}
		#else
			ui8_tx_buffer[3] = 0x46;
			ui8_tx_buffer[4] = 0x46;
		#endif
		
		// fault temperature limit
		// E06 ERROR_OVERTEMPERATURE
		#if ENABLE_TEMPERATURE_ERROR_MIN_LIMIT
		// temperature error at min limit value
		if(((uint8_t) (ui16_motor_temperature_filtered_x10 / 10)) >= ui8_motor_temperature_min_value_to_limit)
		{
			ui8_display_fault_code = ERROR_OVERTEMPERATURE;
		}
		#else
		// temperature error at max limit value
		if(((uint8_t) (ui16_motor_temperature_filtered_x10 / 10)) >= ui8_motor_temperature_max_value_to_limit)
		{
			ui8_display_fault_code = ERROR_OVERTEMPERATURE;
		}
		#endif
	
		// blocked motor error has priority
		if(ui8_system_state == ERROR_MOTOR_BLOCKED)
		{	
			ui8_display_fault_code = ERROR_MOTOR_BLOCKED;
		}	
		else
		{	
			if((ui8_system_state != NO_ERROR)&&(ui8_display_fault_code == NO_FAULT))
				ui8_display_fault_code = ui8_system_state;
		}

		// transmit to display function code or fault code
		if((ui8_display_fault_code != NO_FAULT)&&(ui8_display_function_code == NO_FUNCTION))
		{
			#if ENABLE_XH18
			if(ui8_display_fault_code == ERROR_WRITE_EEPROM)
			{	
				// instead of E09, display blinking E08
				if(ui8_display_blinking_code)
					ui8_tx_buffer[5] = 8;
			}
			else if (ui8_display_fault_code == ERROR_OVERVOLTAGE)
			{	
				// instead of E01, display blinking E06
				if(ui8_display_blinking_code)
					ui8_tx_buffer[5] = 6;
			}
			else if (ui8_display_fault_code == ERROR_MOTOR_CHECK)
			{	
				// instead of E05, display blinking E03
				if(ui8_display_blinking_code)
					ui8_tx_buffer[5] = 3;
			}
			else if (ui8_display_fault_code == ERROR_BATTERY_OVERCURRENT)
			{	
				// instead of E07, display blinking E04
				if(ui8_display_blinking_code)
					ui8_tx_buffer[5] = 4;
			}
			else	
			{
				// fault code
				ui8_tx_buffer[5] = ui8_display_fault_code;
			}
			#else // ENABLE VLCD5/6 or 850C
			if(ui8_auto_display_data_status)
				// display data
				ui8_tx_buffer[5] = CLEAR_DISPLAY;
			else
				// fault code
				ui8_tx_buffer[5] = ui8_display_fault_code;
			#endif
		}
		else if(ui8_display_function_code != NO_FUNCTION)
		{
			// function code
			if((!ui8_menu_flag)&&(ui8_menu_index)&&
			   ((m_configuration_variables.ui8_set_parameter_enabled)||
			   (ui8_assist_level == OFF)||
			   ((ui8_startup_counter < DELAY_MENU_ON)&&(ui8_assist_level == TURBO))))
			{
				// display blinking function code
				if(ui8_display_blinking_code)
					ui8_tx_buffer[5] = ui8_display_function_code;
				else
					// clear code
					ui8_tx_buffer[5] = CLEAR_DISPLAY;
			}
			else
			{
				// display data function code
				ui8_tx_buffer[5] = ui8_display_function_code;
			}
		}
		else
		{
			// clear code
			ui8_tx_buffer[5] = CLEAR_DISPLAY;
		}
		
		// transmit to display data value or wheel speed
		if(ui8_display_data_enabled)
		{
			// data values
			ui16_data_value_array[0] = (uint16_t) ui16_display_data_factor / ui16_motor_temperature_filtered_x10;
			//ui16_data_value_array[0] = (uint16_t) ui16_display_data_factor / (ui8_g_foc_angle * 10);
			//ui16_data_value_array[0] = (uint16_t) ui16_display_data_factor / (ui8_walk_assist_duty_cycle_target * 10);
			//ui16_data_value_array[0] = (uint16_t) ui16_display_data_factor / (ui8_pedal_torque_per_10_bit_ADC_step_calc_x100 * 10);
			ui16_data_value_array[1] = (uint16_t) ui16_display_data_factor / ui16_battery_SOC_percentage_x10;
			ui16_data_value_array[2] = (uint16_t) ui16_display_data_factor / ui16_battery_voltage_calibrated_x10;
			ui16_data_value_array[3] = (uint16_t) ui16_display_data_factor / ui8_battery_current_filtered_x10;
			ui16_data_value_array[4] = (uint16_t) ui16_display_data_factor / (ui16_battery_power_filtered_x10 / 10);
			ui16_data_value_array[5] = (uint16_t) ui16_display_data_factor / UI8_ADC_THROTTLE;
			ui16_data_value_array[6] = (uint16_t) ui16_display_data_factor / UI16_ADC_10_BIT_TORQUE_SENSOR;
			
			if(ui8_pedal_cadence_RPM > 100)
				ui16_data_value_array[7] = (uint16_t) ui16_display_data_factor / ui8_pedal_cadence_RPM;
			else
				ui16_data_value_array[7] = (uint16_t) ui16_display_data_factor / (ui8_pedal_cadence_RPM * 10);
			
			ui16_data_value_array[8] = (uint16_t) ui16_display_data_factor / (ui16_human_power_filtered_x10 / 10);
			ui16_data_value_array[9] = (uint16_t) ui16_display_data_factor / ui16_adc_pedal_torque_delta;
			ui16_data_value_array[10] = (uint16_t) ui16_display_data_factor / (ui32_wh_x10 / 10);
			ui16_data_value_array[11] = (uint16_t) ui16_display_data_factor / (ui16_pedal_weight_x100 / 10);
			ui16_data_value_array[12] = (uint16_t) ui16_display_data_factor / (ui8_pedal_torque_per_10_bit_ADC_step_x100 * 10);
			ui16_data_value_array[13] = (uint16_t) ui16_display_data_factor / FUNCTION_STATUS_OFF;
			ui16_data_value_array[14] = (uint16_t) ui16_display_data_factor / FUNCTION_STATUS_ON;
			ui16_data_value_array[15] = (uint16_t) ui16_display_data_factor / (ui8_display_riding_mode * 100 + DISPLAY_STATUS_OFFSET);
			ui16_data_value_array[16] = (uint16_t) ui16_display_data_factor / (ui8_display_lights_configuration * 100 + DISPLAY_STATUS_OFFSET);
			
			// display data
			if((ui8_display_battery_soc)||((ui8_startup_counter < DELAY_MENU_ON)&&(ui8_assist_level == TURBO)))
				ui16_display_data = ui16_data_value_array[1];
			else if((ui8_display_data_on_startup)&&(ui8_startup_counter < DELAY_MENU_ON)&&(ui8_assist_level != TURBO)&&(!ui8_menu_index))
				ui16_display_data = ui16_data_value_array[DATA_DISPLAY_ON_STARTUP];
			else if(ui8_torque_sensor_calibration_started)
				ui16_display_data = ui16_data_value_array[11];
			else if(ui8_display_torque_sensor_step)
				ui16_display_data = ui16_data_value_array[12];
			else if(ui8_display_torque_sensor_value)
				ui16_display_data = ui16_data_value_array[6];
			else if((ui8_menu_counter <= ui8_delay_display_function)&&(ui8_menu_index)&&((ui8_assist_level < 2)||(ui8_display_alternative_lights_configuration))) // OFF & ECO & alternative lights configuration
				ui16_display_data = ui16_data_value_array[13 + (ui8_display_function_status[ui8_menu_index - 1][ui8_assist_level])];
			else if((ui8_menu_counter <= ui8_delay_display_function)&&(ui8_menu_index)&&(ui8_assist_level == TURBO))
				ui16_display_data = ui16_data_value_array[16];
			else if((ui8_menu_counter <= ui8_delay_display_function)&&(ui8_menu_index))
				ui16_display_data = ui16_data_value_array[15];
			else
				ui16_display_data = ui16_data_value_array[ui8_data_index_array[ui8_data_index]];

			// todo filter for 500C display
			
			// valid value
			if(ui16_display_data)
			{
				#if UNITS_TYPE	// mph and miles
				uint16_t ui16_data_value = ui16_display_data_factor / ui16_display_data;
				if(ui16_data_value > 624)
					ui16_display_data = ui16_display_data * 10;
				#endif
				ui8_tx_buffer[6] = (uint8_t) (ui16_display_data & 0xFF);
				ui8_tx_buffer[7] = (uint8_t) (ui16_display_data >> 8);
			}
			else
			{	
				ui8_tx_buffer[6] = 0x07;
				ui8_tx_buffer[7] = 0x07;
			}
		}
		else
		{	
			// wheel speed
			if(ui16_oem_wheel_speed)
			{
				ui8_tx_buffer[6] = (uint8_t) (ui16_oem_wheel_speed & 0xFF);
				ui8_tx_buffer[7] = (uint8_t) (ui16_oem_wheel_speed >> 8);
			}
			else
			{
				ui8_tx_buffer[6] = 0x07;
				ui8_tx_buffer[7] = 0x07;
			}
		}
				
		// set working flag
		#if ENABLE_DISPLAY_ALWAYS_ON
		// set working flag used to hold display always on
		ui8_working_status |= 0x04;
		#endif
		
		#if ENABLE_DISPLAY_WORKING_FLAG
		// wheel turning
		if(ui16_oem_wheel_speed)
			// bit7 = 1 (wheel turning)
			ui8_working_status |= 0x80;
		else
			// bit7 = 0 (wheel not turning)
				ui8_working_status &= 0x7F;
		
		// motor working
		if (ui8_g_duty_cycle > 10)
			// bit6 = 1 (motor working)
			ui8_working_status |= 0x40;
		else
			// bit6 = 0 (motor not working)
			ui8_working_status &= 0xBF;
		
		// motor working or wheel turning?
		if(ui8_working_status & 0xC0)
		{
			// set working flag used by display
			ui8_working_status |= 0x04;
		}
		else
		{
			// clear working flag used by display
			ui8_working_status &= 0xFB;
		}
		#endif

		// working status
		ui8_tx_buffer[2] = (ui8_working_status & 0x1F);
		
		// clear motor working, wheel turning and working flags
		ui8_working_status &= 0x3B;	
		
		// prepare check code of the package
		ui8_tx_check_code = 0x00;
		for(ui8_i = 0; ui8_i < TX_CHECK_CODE; ui8_i++)
		{
			ui8_tx_check_code += ui8_tx_buffer[ui8_i];
		}
		ui8_tx_buffer[TX_CHECK_CODE] = ui8_tx_check_code;

		// send the full package to UART
		for(ui8_i = 0; ui8_i < UART_TX_BUFFER_LEN; ui8_i++)
		{
			uart_put_char(ui8_tx_buffer[ui8_i]);
		}
	}
}

	
static void calc_oem_wheel_speed(void)
{ 
	if(ui8_display_ready_flag)
	{
		uint32_t ui32_oem_wheel_speed;
		uint32_t ui32_oem_wheel_perimeter;
			
		// calc oem wheel speed (wheel turning time)
		if(ui16_wheel_speed_sensor_ticks)
		{
			ui32_oem_wheel_speed = ((uint32_t) ui16_wheel_speed_sensor_ticks * 10) / OEM_WHEEL_SPEED_DIVISOR;
			
			// speed conversion for different perimeter			
			ui32_oem_wheel_perimeter = ((uint32_t) ui8_oem_wheel_diameter * 7975) / 100; // 25.4 * 3.14 * 100 = 7975
			ui32_oem_wheel_speed *= ui32_oem_wheel_perimeter;
			ui32_oem_wheel_speed /= (uint32_t) m_configuration_variables.ui16_wheel_perimeter;
			
			// oem wheel speed (wheel turning time)
			ui16_oem_wheel_speed = (uint16_t) ui32_oem_wheel_speed;
		}
		else
		{
			ui16_oem_wheel_speed = 0;
		}
	}
	
	#if ENABLE_ODOMETER_COMPENSATION
	uint16_t ui16_wheel_speed;
	uint16_t ui16_data_speed;
	uint16_t ui16_speed_difference;

	// calc wheel speed  mm/0.1 sec
	if(ui16_oem_wheel_speed)
		ui16_wheel_speed = (uint16_t)((ui16_display_data_factor / ui16_oem_wheel_speed) * ((uint16_t) 100 / 36));
	else
		ui16_wheel_speed = 0;

	// calc data speed  mm/0.1 sec
	if(ui16_display_data)	
		ui16_data_speed = (uint16_t)((ui16_display_data_factor / ui16_display_data) * ((uint16_t) 100 / 36));
	else
		ui16_data_speed = 0;
	
	// calc odometer difference
	if(ui8_display_data_enabled)
	{	
		if(ui16_data_speed > ui16_wheel_speed)
		{	
			// calc + speed difference mm/0.1 sec
			ui16_speed_difference = ui16_data_speed - ui16_wheel_speed;
			// add difference to odometer
			ui32_odometer_compensation_mm += (uint32_t) ui16_speed_difference;
		}
		else
		{	
			// calc - speed difference mm/0.1 sec
			ui16_speed_difference = ui16_wheel_speed - ui16_data_speed;
				// subtracts difference from odometer
			ui32_odometer_compensation_mm -= (uint32_t) ui16_speed_difference;
		}
	}
	else
	{	
		// odometer compensation
		if((ui16_wheel_speed)&&(ui32_odometer_compensation_mm > ZERO_ODOMETER_COMPENSATION))
		{
			ui32_odometer_compensation_mm -= (uint32_t) ui16_wheel_speed;
			ui16_oem_wheel_speed = 0;
		}
	}
	#endif
} 


static void check_battery_soc(void)
{
	static uint8_t ui8_no_load_counter = 20;
	uint16_t ui16_battery_voltage_x10;
	uint16_t ui16_battery_SOC_used_x10;
	uint16_t ui16_actual_battery_SOC_x10;
	uint16_t ui16_battery_power_x10;
	
	// battery voltage x10
	ui16_battery_voltage_x10 = (ui16_battery_voltage_filtered_x1000) / 100;
	
	// filter battery voltage x10
	ui16_battery_voltage_filtered_x10 = filter(ui16_battery_voltage_x10, ui16_battery_voltage_filtered_x10, 2);

	// save no load voltage x10 if current is < adc current min for 2 seconds
	if(ui8_adc_battery_current_filtered < 2) {
		if(++ui8_no_load_counter > 20) {
			ui16_battery_no_load_voltage_filtered_x10 = ui16_battery_voltage_x10;
			ui8_no_load_counter--;
		}
	}
	else {
		ui8_no_load_counter = 0;
	}

	// filter battery voltage soc x10
	ui16_battery_voltage_soc_filtered_x10 = filter(ui16_battery_no_load_voltage_filtered_x10, ui16_battery_voltage_soc_filtered_x10, 2);

	#if ENABLE_VLCD6 || ENABLE_XH18
	if(ui16_battery_voltage_soc_filtered_x10 > ((uint16_t) (BATTERY_CELLS_NUMBER * LI_ION_CELL_VOLTS_6_X100 / 10))) {ui8_battery_state_of_charge = 7;}		// overvoltage
	else if(ui16_battery_voltage_soc_filtered_x10 > ((uint16_t) (BATTERY_CELLS_NUMBER * LI_ION_CELL_VOLTS_5_X100 / 10))) {ui8_battery_state_of_charge = 6;}	// 4 bars -> SOC reset
	else if(ui16_battery_voltage_soc_filtered_x10 > ((uint16_t) (BATTERY_CELLS_NUMBER * LI_ION_CELL_VOLTS_4_X100 / 10))) {ui8_battery_state_of_charge = 5;}	// 4 bars -> full
	else if(ui16_battery_voltage_soc_filtered_x10 > ((uint16_t) (BATTERY_CELLS_NUMBER * LI_ION_CELL_VOLTS_3_X100 / 10))) {ui8_battery_state_of_charge = 4;}	// 3 bars
	else if(ui16_battery_voltage_soc_filtered_x10 > ((uint16_t) (BATTERY_CELLS_NUMBER * LI_ION_CELL_VOLTS_2_X100 / 10))) {ui8_battery_state_of_charge = 3;}	// 2 bars
	else if(ui16_battery_voltage_soc_filtered_x10 > ((uint16_t) (BATTERY_CELLS_NUMBER * LI_ION_CELL_VOLTS_1_X100 / 10))) {ui8_battery_state_of_charge = 2;}	// 1 bar
	else if(ui16_battery_voltage_soc_filtered_x10 > ((uint16_t) (BATTERY_CELLS_NUMBER * LI_ION_CELL_VOLTS_0_X100 / 10))) {ui8_battery_state_of_charge = 1;}	// blink -> empty
	else {ui8_battery_state_of_charge = 0;} // undervoltage
	#else // ENABLE_VLCD5 or 850C
	if(ui16_battery_voltage_soc_filtered_x10 > ((uint16_t) (BATTERY_CELLS_NUMBER * LI_ION_CELL_VOLTS_8_X100 / 10))) {ui8_battery_state_of_charge = 9;}   		// overvoltage
	else if(ui16_battery_voltage_soc_filtered_x10 > ((uint16_t) (BATTERY_CELLS_NUMBER * LI_ION_CELL_VOLTS_7_X100 / 10))) {ui8_battery_state_of_charge = 8;}	// 6 bars -> SOC reset
	else if(ui16_battery_voltage_soc_filtered_x10 > ((uint16_t) (BATTERY_CELLS_NUMBER * LI_ION_CELL_VOLTS_6_X100 / 10))) {ui8_battery_state_of_charge = 7;}	// 6 bars -> full
	else if(ui16_battery_voltage_soc_filtered_x10 > ((uint16_t) (BATTERY_CELLS_NUMBER * LI_ION_CELL_VOLTS_5_X100 / 10))) {ui8_battery_state_of_charge = 6;}	// 5 bars
	else if(ui16_battery_voltage_soc_filtered_x10 > ((uint16_t) (BATTERY_CELLS_NUMBER * LI_ION_CELL_VOLTS_4_X100 / 10))) {ui8_battery_state_of_charge = 5;}	// 4 bars
	else if(ui16_battery_voltage_soc_filtered_x10 > ((uint16_t) (BATTERY_CELLS_NUMBER * LI_ION_CELL_VOLTS_3_X100 / 10))) {ui8_battery_state_of_charge = 4;}	// 3 bars
	else if(ui16_battery_voltage_soc_filtered_x10 > ((uint16_t) (BATTERY_CELLS_NUMBER * LI_ION_CELL_VOLTS_2_X100 / 10))) {ui8_battery_state_of_charge = 3;}	// 2 bars
	else if(ui16_battery_voltage_soc_filtered_x10 > ((uint16_t) (BATTERY_CELLS_NUMBER * LI_ION_CELL_VOLTS_1_X100 / 10))) {ui8_battery_state_of_charge = 2;}	// 1 bar
	else if(ui16_battery_voltage_soc_filtered_x10 > ((uint16_t) (BATTERY_CELLS_NUMBER * LI_ION_CELL_VOLTS_0_X100 / 10))) {ui8_battery_state_of_charge = 1;}	// blink -> empty
	else {ui8_battery_state_of_charge = 0;} // undervoltage
	#endif
	
	// battery voltage calibrated x10 for display data
	ui16_battery_voltage_calibrated_x10 = (ui16_battery_voltage_filtered_x10 * ACTUAL_BATTERY_VOLTAGE_PERCENT) / 100;
	
	// battery power x 10
	ui16_battery_power_x10 = (uint16_t)(((uint32_t) ui16_battery_voltage_filtered_x10 * ui8_battery_current_filtered_x10) / 10);
	// battery power filtered x 10 for display data
	ui16_battery_power_filtered_x10 = filter(ui16_battery_power_x10, ui16_battery_power_filtered_x10, 8);
	
	// consumed watt-hours
	ui32_wh_sum_x10 += ui16_battery_power_x10;
	// calculate watt-hours X10 since power on
	ui32_wh_since_power_on_x10 = ui32_wh_sum_x10 / 32400; // 36000 -10% for calibration
	// calculate watt-hours X10 since last full charge
	ui32_wh_x10 = ui32_wh_x10_offset + ui32_wh_since_power_on_x10;
	
	// calculate and set remaining percentage x10
	if(m_configuration_variables.ui8_soc_percent_calculation == 2) { // Volts
			ui16_battery_SOC_percentage_x10 = read_battery_soc();
	}
	else { // Auto or Wh
		// calculate percentage battery capacity used x10
		ui16_battery_SOC_used_x10 = (uint16_t)(((uint32_t) ui32_wh_x10 * 100) / ui16_actual_battery_capacity);

		// limit used percentage to 100 x10
		if(ui16_battery_SOC_used_x10 > 1000)
			ui16_battery_SOC_used_x10 = 1000;
		
		ui16_battery_SOC_percentage_x10 = 1000 - ui16_battery_SOC_used_x10;
	}
	
	// convert remaining percentage x10 to 8 bit
	m_configuration_variables.ui8_battery_SOC_percentage_8b = (uint8_t)(ui16_battery_SOC_percentage_x10 >> 2);
	
	// automatic set battery percentage x10 (full charge)
	if((ui8_display_ready_flag))
	{	
		if((!ui8_battery_SOC_init_flag)||
		  ((!ui8_battery_SOC_reset_flag)&&(ui16_battery_voltage_filtered_x10 > BATTERY_VOLTAGE_RESET_SOC_PERCENT_X10)))
		{
			ui16_battery_SOC_percentage_x10 = 1000;
			ui32_wh_x10_offset = 0;
			ui8_battery_SOC_init_flag = 1;
		}
		
		// battery SOC reset flag
		if((ui8_battery_SOC_init_flag)
		  &&(!ui8_battery_SOC_reset_flag)
		  &&(ui8_startup_counter++ > DELAY_MENU_ON))
		{
			ui16_actual_battery_SOC_x10 = read_battery_soc();
			
			// check soc percentage
			if((m_configuration_variables.ui8_soc_percent_calculation == 0) // Auto
				&& (ui16_battery_SOC_percentage_x10 > BATTERY_SOC_PERCENT_THRESHOLD_X10)
				&& ((ui16_actual_battery_SOC_x10 < (ui16_battery_SOC_percentage_x10 - BATTERY_SOC_PERCENT_THRESHOLD_X10))
				|| (ui16_actual_battery_SOC_x10 > (ui16_battery_SOC_percentage_x10 + BATTERY_SOC_PERCENT_THRESHOLD_X10)))) {
					ui16_battery_SOC_percentage_x10 = ui16_actual_battery_SOC_x10;
					
					// calculate watt-hours x10
					ui32_wh_x10_offset = ((uint32_t) (1000 - ui16_battery_SOC_percentage_x10) * ui16_actual_battery_capacity) / 100;
					
					// for display soc %
					ui8_display_battery_soc = 1;
					ui8_display_data_enabled = 1;
					ui8_startup_counter = DELAY_MENU_ON >> 1;
					ui8_menu_counter = DELAY_MENU_ON >> 1;
			}
			else {
				ui8_battery_SOC_reset_flag = 1;
			}
		}
	}
}


// read battery percentage x10 (actual charge)
uint16_t read_battery_soc(void)
{
	uint16_t ui16_battery_SOC_calc_x10 = 0;
	
	#if ENABLE_VLCD6 || ENABLE_XH18
	switch(ui8_battery_state_of_charge)
	{
		case 0:	ui16_battery_SOC_calc_x10 = 10; break;  // undervoltage
		case 1:	ui16_battery_SOC_calc_x10 = calc_battery_soc_x10(1, 250, LI_ION_CELL_VOLTS_1_X100, LI_ION_CELL_VOLTS_0_X100); break; // blink - empty
		case 2:	ui16_battery_SOC_calc_x10 = calc_battery_soc_x10(250, 250, LI_ION_CELL_VOLTS_2_X100, LI_ION_CELL_VOLTS_1_X100); break; // 1 bars
		case 3:	ui16_battery_SOC_calc_x10 = calc_battery_soc_x10(500, 250, LI_ION_CELL_VOLTS_3_X100, LI_ION_CELL_VOLTS_2_X100); break; // 2 bars
		case 4:	ui16_battery_SOC_calc_x10 = calc_battery_soc_x10(750, 250, LI_ION_CELL_VOLTS_4_X100, LI_ION_CELL_VOLTS_3_X100); break; // 3 bars
		case 5:	ui16_battery_SOC_calc_x10 = 1000; break; // 4 bars - full
		case 6:	ui16_battery_SOC_calc_x10 = 1000; break; // 4 bars - SOC reset
		case 7:	ui16_battery_SOC_calc_x10 = 1000; break; // overvoltage
	}
	#else // ENABLE_VLCD5 or 850C
	switch(ui8_battery_state_of_charge)
	{
		case 0:	ui16_battery_SOC_calc_x10 = 10; break;  // undervoltage
		case 1:	ui16_battery_SOC_calc_x10 = calc_battery_soc_x10(1, 167, LI_ION_CELL_VOLTS_1_X100, LI_ION_CELL_VOLTS_0_X100); break; // blink - empty
		case 2:	ui16_battery_SOC_calc_x10 = calc_battery_soc_x10(167, 167, LI_ION_CELL_VOLTS_2_X100, LI_ION_CELL_VOLTS_1_X100); break; // 1 bars
		case 3:	ui16_battery_SOC_calc_x10 = calc_battery_soc_x10(334, 167, LI_ION_CELL_VOLTS_3_X100, LI_ION_CELL_VOLTS_2_X100); break; // 2 bars
		case 4:	ui16_battery_SOC_calc_x10 = calc_battery_soc_x10(500, 167, LI_ION_CELL_VOLTS_4_X100, LI_ION_CELL_VOLTS_3_X100); break; // 3 bars
		case 5:	ui16_battery_SOC_calc_x10 = calc_battery_soc_x10(667, 167, LI_ION_CELL_VOLTS_5_X100, LI_ION_CELL_VOLTS_4_X100); break; // 4 bars
		case 6:	ui16_battery_SOC_calc_x10 = calc_battery_soc_x10(834, 167, LI_ION_CELL_VOLTS_6_X100, LI_ION_CELL_VOLTS_5_X100); break; // 5 bars
		case 7:	ui16_battery_SOC_calc_x10 = 1000; break; // 6 bars - full
		case 8:	ui16_battery_SOC_calc_x10 = 1000; break; // 6 bars - SOC reset
		case 9:	ui16_battery_SOC_calc_x10 = 1000; break; // overvoltage
	}	
	#endif
	
	return ui16_battery_SOC_calc_x10;
}


// calculate battery soc percentage x10
uint16_t calc_battery_soc_x10(uint16_t ui16_battery_soc_offset_x10, uint16_t ui16_battery_soc_step_x10, uint16_t ui16_cell_volts_max_x100, uint16_t ui16_cell_volts_min_x100)
{
#define CELL_VOLTS_CALIBRATION				8
	
	uint16_t ui16_cell_voltage_soc_x100 = ui16_battery_voltage_soc_filtered_x10 * 10 / BATTERY_CELLS_NUMBER;
	
	uint16_t ui16_battery_soc_calc_temp_x10 = (ui16_battery_soc_offset_x10 + (ui16_battery_soc_step_x10 * (ui16_cell_voltage_soc_x100 - ui16_cell_volts_min_x100) / (ui16_cell_volts_max_x100 - ui16_cell_volts_min_x100 + CELL_VOLTS_CALIBRATION)));
	
	return ui16_battery_soc_calc_temp_x10;
}
