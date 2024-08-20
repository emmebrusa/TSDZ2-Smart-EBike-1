/*
 * TongSheng TSDZ2 motor controller firmware/
 *
 * Copyright (C) Casainho, Leon, MSpider65 2020.
 *
 * Released under the GPL License, Version 3
 */

#ifndef EBIKE_APP_H_
#define EBIKE_APP_H_

#include <stdint.h>

// startup boost mode
#define CADENCE						0
#define SPEED						1

// for oem display
extern volatile uint8_t ui8_display_fault_code;
extern volatile uint8_t ui8_system_state;

// cadence sensor
extern volatile uint16_t ui16_cadence_ticks_count_min_speed_adj;

// Torque sensor coaster brake engaged threshold value
extern volatile uint16_t ui16_adc_coaster_brake_threshold;

// ADC motor phase current max
extern volatile uint8_t ui8_adc_motor_phase_current_max;

// Motor enabled
extern volatile uint8_t ui8_motor_enabled;

typedef struct _configuration_variables
{
  //uint8_t ui8_motor_power_x10; // not used
  uint8_t ui8_battery_current_max; // from  ebike_app.c
  uint16_t ui16_battery_low_voltage_cut_off_x10;
  uint16_t ui16_wheel_perimeter;
  uint8_t ui8_wheel_speed_max;
  uint8_t ui8_motor_type;
  uint8_t ui8_avaiable_for_future_use;
  // for oem display
  uint8_t ui8_assist_without_pedal_rotation_enabled;
  uint8_t ui8_assist_with_error_enabled;
  uint8_t ui8_battery_SOC_percentage_8b;
  uint8_t ui8_set_parameter_enabled;
  uint8_t ui8_street_mode_enabled;
  uint8_t ui8_riding_mode;
  uint8_t ui8_lights_configuration;
  uint8_t ui8_startup_boost_enabled;
  uint8_t ui8_auto_display_data_enabled;
  uint8_t ui8_torque_sensor_adv_enabled; 
  uint8_t ui8_soc_percent_calculation;
} struct_configuration_variables;

void ebike_app_controller(void);
struct_configuration_variables* get_configuration_variables(void);

void ebike_app_init(void);

#endif /* _EBIKE_APP_H_ */
