/*
 * TongSheng TSDZ2 motor controller firmware/
 *
 * Copyright (C) Casainho, Leon, MSpider65 2020.
 *
 * Released under the GPL License, Version 3
 */

#ifndef _MAIN_H_
#define _MAIN_H_

#include "config.h"
#include "common.h"

//#define FW_VERSION 7 // mspider65

// PWM related values
// motor
#define PWM_CYCLES_SECOND                                       19047U // 52us (PWM period)
#define PWM_CYCLES_COUNTER_MAX                                  3800U  // 5 erps minimum speed -> 1/5 = 200 ms; 200 ms / 50 us = 4000 (3125 at 15.625KHz)
#define DOUBLE_PWM_CYCLES_SECOND                                38094 // 25us (2 irq x PWM period)
// ramp up/down PWM cycles count
#define PWM_DUTY_CYCLE_RAMP_UP_INVERSE_STEP_CADENCE_OFFSET      60     // PWM_DUTY_CYCLE_RAMP_UP_INVERSE_STEP offset for cadence assist mode
//#define PWM_DUTY_CYCLE_RAMP_UP_INVERSE_STEP_DEFAULT             195    // 160 -> 160 * 64 us for every duty cycle increment at 15.625KHz
//#define PWM_DUTY_CYCLE_RAMP_UP_INVERSE_STEP_MIN                 24     // 20 -> 20 * 64 us for every duty cycle increment at 15.625KHz
#define PWM_DUTY_CYCLE_RAMP_UP_INVERSE_STEP_DEFAULT             160    // 160 -> 160 * 64 us for every duty cycle increment at 15.625KHz
#define PWM_DUTY_CYCLE_RAMP_UP_INVERSE_STEP_MIN                 20     // 20 -> 20 * 64 us for every duty cycle increment at 15.625KHz
#define PWM_DUTY_CYCLE_RAMP_DOWN_INVERSE_STEP_DEFAULT           49     // 40 -> 40 * 64 us for every duty cycle decrement at 15.625KHz
#define PWM_DUTY_CYCLE_RAMP_DOWN_INVERSE_STEP_MIN               10     // 8 -> 8 * 64 us for every duty cycle decrement at 15.625KHz
#define MOTOR_OVER_SPEED_ERPS                                   650    // motor max speed | 30 points for the sinewave at max speed (less than PWM_CYCLES_SECOND/30)
#define CRUISE_DUTY_CYCLE_RAMP_UP_INVERSE_STEP                  98    // 80 at 15.625KHz
#define WALK_ASSIST_DUTY_CYCLE_RAMP_UP_INVERSE_STEP             255    // 200 at 15.625KHz
#define THROTTLE_DUTY_CYCLE_RAMP_UP_INVERSE_STEP_DEFAULT        98    // 80 at 15.625KHz
#define THROTTLE_DUTY_CYCLE_RAMP_UP_INVERSE_STEP_MIN            49     // 40 at 15.625KHz

#define MOTOR_SPEED_FIELD_WEAKENING_MIN          				300		// ERPS

// cadence
#define CADENCE_SENSOR_CALC_COUNTER_MIN                         4266  // 3500 at 15.625KHz
#define CADENCE_SENSOR_TICKS_COUNTER_MIN_AT_SPEED               341  // 280 at 15.625KHz
#define CADENCE_TICKS_STARTUP                                   7618  // ui16_cadence_sensor_ticks value for startup. About 7-8 RPM (6250 at 15.625KHz)
#define CADENCE_SENSOR_STANDARD_MODE_SCHMITT_TRIGGER_THRESHOLD  0   // software based Schmitt trigger to stop motor jitter when at resolution limits (350 at 15.625KHz)
// Wheel speed sensor
#define WHEEL_SPEED_SENSOR_TICKS_COUNTER_MAX                    165   // (135 at 15,625KHz) something like 200 m/h with a 6'' wheel
#define WHEEL_SPEED_SENSOR_TICKS_COUNTER_MIN                    39976 // could be a bigger number but will make for a slow detection of stopped wheel speed


#define PWM_DUTY_CYCLE_MAX                                        255U
#define PWM_DUTY_CYCLE_BITS                                       8U
#define MIDDLE_SVM_TABLE                                          106
#define MIDDLE_PWM_COUNTER                                        105

/*---------------------------------------------------------
 NOTE: regarding duty cycle (PWM) ramping

 Do not change these values if not sure of the effects!

 A lower value of the duty cycle inverse step will mean
 a faster acceleration. Be careful not to choose too
 low values for acceleration.
 ---------------------------------------------------------*/

#define MOTOR_ROTOR_OFFSET_ANGLE                                  10
#define MOTOR_ROTOR_ANGLE_30                                      (20  + MOTOR_ROTOR_OFFSET_ANGLE)
#define MOTOR_ROTOR_ANGLE_90                                      (63  + MOTOR_ROTOR_OFFSET_ANGLE)
#define MOTOR_ROTOR_ANGLE_150                                     (106 + MOTOR_ROTOR_OFFSET_ANGLE)
#define MOTOR_ROTOR_ANGLE_210                                     (148 + MOTOR_ROTOR_OFFSET_ANGLE)
#define MOTOR_ROTOR_ANGLE_270                                     (191 + MOTOR_ROTOR_OFFSET_ANGLE)
#define MOTOR_ROTOR_ANGLE_330                                     (233 + MOTOR_ROTOR_OFFSET_ANGLE)

/*---------------------------------------------------------
 NOTE: regarding motor rotor offset

 The motor rotor offset should be as close to 0 as
 possible. You can try to tune with the wheel in the air,
 full throttle and look at the batttery current. Adjust
 for the lowest battery current possible.
 ---------------------------------------------------------*/

#define MOTOR_ROTOR_ERPS_START_INTERPOLATION_60_DEGREES           10

/*---------------------------------------------------------
 NOTE: regarding motor start interpolation

 This value is the ERPS speed after which a transition
 happens from sinewave and no interpolation to
 interpolation 60 degrees. Must be found experimentally
 but a value of 25 may be good.
 ---------------------------------------------------------*/
 
// adc torque offset gap value for error
#define ADC_TORQUE_SENSOR_OFFSET_THRESHOLD		30

// Torque sensor range values
#define ADC_TORQUE_SENSOR_RANGE					(uint16_t)(PEDAL_TORQUE_ADC_MAX - PEDAL_TORQUE_ADC_OFFSET)
#define ADC_TORQUE_SENSOR_RANGE_TARGET	  		160

// Torque sensor offset values
#if TORQUE_SENSOR_CALIBRATED
#define ADC_TORQUE_SENSOR_CALIBRATION_OFFSET    (uint16_t)(((6 * ADC_TORQUE_SENSOR_RANGE) / ADC_TORQUE_SENSOR_RANGE_TARGET) + 1)
#define ADC_TORQUE_SENSOR_MIDDLE_OFFSET_ADJ		(uint16_t)(((20 * ADC_TORQUE_SENSOR_RANGE) / ADC_TORQUE_SENSOR_RANGE_TARGET) + 1)
#define ADC_TORQUE_SENSOR_OFFSET_ADJ			(uint16_t)(((PEDAL_TORQUE_ADC_OFFSET_ADJ * ADC_TORQUE_SENSOR_RANGE) / ADC_TORQUE_SENSOR_RANGE_TARGET) + 1)
#else
#define ADC_TORQUE_SENSOR_CALIBRATION_OFFSET    6
#define ADC_TORQUE_SENSOR_MIDDLE_OFFSET_ADJ		20
#define ADC_TORQUE_SENSOR_OFFSET_ADJ			PEDAL_TORQUE_ADC_OFFSET_ADJ
#endif

// adc torque range parameters for remapping
#define ADC_TORQUE_SENSOR_DELTA_ADJ				(uint16_t)((ADC_TORQUE_SENSOR_MIDDLE_OFFSET_ADJ * 2) - ADC_TORQUE_SENSOR_CALIBRATION_OFFSET - ADC_TORQUE_SENSOR_OFFSET_ADJ)
#define ADC_TORQUE_SENSOR_RANGE_INGREASE_X100	(uint16_t)((ADC_TORQUE_SENSOR_RANGE_TARGET * 50) / ADC_TORQUE_SENSOR_RANGE)
#define ADC_TORQUE_SENSOR_ANGLE_COEFF			11
#define ADC_TORQUE_SENSOR_ANGLE_COEFF_X10		(uint16_t)(ADC_TORQUE_SENSOR_ANGLE_COEFF * 10)

#define ADC_TORQUE_SENSOR_RANGE_TARGET_MIN 		(uint16_t)((float)((ADC_TORQUE_SENSOR_RANGE_TARGET / 2) \
* (((ADC_TORQUE_SENSOR_RANGE_TARGET / 2) / ADC_TORQUE_SENSOR_ANGLE_COEFF + ADC_TORQUE_SENSOR_ANGLE_COEFF) / ADC_TORQUE_SENSOR_ANGLE_COEFF)))

#define ADC_TORQUE_SENSOR_RANGE_TARGET_MAX 		(uint16_t)((ADC_TORQUE_SENSOR_RANGE_TARGET_MIN * (100 + PEDAL_TORQUE_ADC_RANGE_ADJ)) / 100)

// parameters of the adc torque step for human power calculation
#define PEDAL_TORQUE_PER_10_BIT_ADC_STEP_BASE_X100	34 // base adc step for remapping
#define WEIGHT_ON_PEDAL_FOR_STEP_CALIBRATION		24 // Kg
#define PERCENT_TORQUE_SENSOR_RANGE_WITH_WEIGHT		75 // % of torque sensor range with weight
#define ADC_TORQUE_SENSOR_TARGET_WITH_WEIGHT		(uint16_t)((ADC_TORQUE_SENSOR_RANGE_TARGET * PERCENT_TORQUE_SENSOR_RANGE_WITH_WEIGHT) / 100)

#define ADC_TORQUE_SENSOR_DELTA_WITH_WEIGHT			(uint16_t)(((((ADC_TORQUE_SENSOR_TARGET_WITH_WEIGHT \
* ADC_TORQUE_SENSOR_RANGE_TARGET_MIN) / ADC_TORQUE_SENSOR_RANGE_TARGET)	* (100 + PEDAL_TORQUE_ADC_RANGE_ADJ) / 100) \
* (ADC_TORQUE_SENSOR_TARGET_WITH_WEIGHT - ADC_TORQUE_SENSOR_CALIBRATION_OFFSET + ADC_TORQUE_SENSOR_OFFSET_ADJ \
- ((ADC_TORQUE_SENSOR_DELTA_ADJ * ADC_TORQUE_SENSOR_TARGET_WITH_WEIGHT) / ADC_TORQUE_SENSOR_RANGE_TARGET))) / ADC_TORQUE_SENSOR_TARGET_WITH_WEIGHT)

#define PEDAL_TORQUE_PER_10_BIT_ADC_STEP_CALC_X100	(uint8_t)((uint16_t)(((WEIGHT_ON_PEDAL_FOR_STEP_CALIBRATION * 167) \
/ ((ADC_TORQUE_SENSOR_DELTA_WITH_WEIGHT * ADC_TORQUE_SENSOR_RANGE_TARGET_MAX) \
/ (ADC_TORQUE_SENSOR_RANGE_TARGET_MAX - (((ADC_TORQUE_SENSOR_RANGE_TARGET_MAX - ADC_TORQUE_SENSOR_DELTA_WITH_WEIGHT) * 10) \
/ PEDAL_TORQUE_ADC_ANGLE_ADJ))) \
* PEDAL_TORQUE_PER_10_BIT_ADC_STEP_ADV_X100) / PEDAL_TORQUE_PER_10_BIT_ADC_STEP_BASE_X100))

// scale the torque assist target current
#define TORQUE_ASSIST_FACTOR_DENOMINATOR			110

/*---------------------------------------------------------
 ---------------------------------------------------------*/
 
#define ADC_10_BIT_BATTERY_CURRENT_MAX				112	// 18 amps
#define ADC_10_BIT_MOTOR_PHASE_CURRENT_MAX			187	// 30 amps
//#define ADC_10_BIT_BATTERY_CURRENT_MAX				106	// 17 amps
//#define ADC_10_BIT_MOTOR_PHASE_CURRENT_MAX			177	// 28 amps// 1 = 0.16 Amp

/*---------------------------------------------------------
 NOTE: regarding ADC battery current max

 This is the maximum current in ADC steps that the motor
 will be able to draw from the battery. A higher value
 will give higher torque figures but the limit of the
 controller is 16 A and it should not be exceeded.
 ---------------------------------------------------------*/

// throttle ADC values
//#define ADC_THROTTLE_MIN_VALUE                                    47
//#define ADC_THROTTLE_MAX_VALUE                                    176

/*---------------------------------------------------------
 NOTE: regarding throttle ADC values

 Max voltage value for throttle, in ADC 8 bits step,
 each ADC 8 bits step = (5 V / 256) = 0.0195

 ---------------------------------------------------------*/

// cadence sensor
#define CADENCE_SENSOR_NUMBER_MAGNETS                           20U

/*-------------------------------------------------------------------------------
 NOTE: regarding the cadence sensor

 CADENCE_SENSOR_NUMBER_MAGNETS = 20, this is the number of magnets used for
 the cadence sensor. Was validated on August 2018 by Casainho and jbalat

 Cadence is calculated by counting how much time passes between two
 transitions. Depending on if all transitions are measured or simply
 transitions of the same kind it is important to adjust the calculation of
 pedal cadence.
 -------------------------------------------------------------------------------*/


// default values
#define DEFAULT_VALUE_BATTERY_CURRENT_MAX                         10  // 10 amps

/*---------------------------------------------------------

 NOTE: regarding the torque sensor output values

 Torque (force) value needs to be found experimentaly.

 One torque sensor ADC 10 bit step is equal to 0.38 kg

 Force (Nm) = 1 Kg * 9.81 * 0.17 (0.17 = arm cranks size)
 ---------------------------------------------------------*/

// ADC battery voltage measurement
#define BATTERY_VOLTAGE_PER_10_BIT_ADC_STEP_X512                  44
#define BATTERY_VOLTAGE_PER_10_BIT_ADC_STEP_X1000                 87  // conversion value verified with a cheap power meter

/*---------------------------------------------------------
 NOTE: regarding ADC battery voltage measurement

 0.344 per ADC 8 bit step:

 17.9 V -->  ADC 8 bits value  = 52;
 40 V   -->  ADC 8 bits value  = 116;

 This signal is atenuated by the opamp 358.
 ---------------------------------------------------------*/

// ADC battery current measurement
#define BATTERY_CURRENT_PER_10_BIT_ADC_STEP_X512                  80
#define BATTERY_CURRENT_PER_10_BIT_ADC_STEP_X100                  16  // 0.16A x 10 bit ADC step

// for oem display

// UART
#define UART_RX_BUFFER_LEN   						7
#define RX_CHECK_CODE					(UART_RX_BUFFER_LEN - 1)															
#define UART_TX_BUFFER_LEN							9
#define TX_CHECK_CODE					(UART_TX_BUFFER_LEN - 1)
#define TX_STX										0x43
#define RX_STX										0x59

// parameters for display data
#if UNITS_TYPE          // 1 mph and miles
#define OEM_WHEEL_FACTOR							900
#else                   // 0 = km/h and kilometer
#define OEM_WHEEL_FACTOR							1435
#endif

#define DATA_INDEX_ARRAY_DIM						6
#define DATA_VALUE_ARRAY_DIM						17

// delay lights function (0.1 sec)
#define DELAY_LIGHTS_ON					 	DELAY_MENU_ON

// delay function status (0.1 sec)
#define DELAY_FUNCTION_STATUS			(uint8_t) (DELAY_MENU_ON / 2)

// delay torque sensor calibration (0.1 sec)
#define DELAY_DISPLAY_TORQUE_CALIBRATION			250

// display function status
#define FUNCTION_STATUS_OFF							1
#define FUNCTION_STATUS_ON				(uint8_t) (100 + DISPLAY_STATUS_OFFSET)
#define DISPLAY_STATUS_OFFSET						5

// assist level 
#define OFF											0
#define ECO											1
#define TOUR										2
#define SPORT										3
#define TURBO										4

// assist pedal level mask
#define ASSIST_PEDAL_LEVEL0							0x10
#define ASSIST_PEDAL_LEVEL01						0x80
#define ASSIST_PEDAL_LEVEL1							0x40
#define ASSIST_PEDAL_LEVEL2							0x02
#define ASSIST_PEDAL_LEVEL3							0x04
#define ASSIST_PEDAL_LEVEL4							0x08

#define ASSIST_PEDAL_LEVEL01_PERCENT				70

// oem display fault & function code
#define CLEAR_DISPLAY								0
#define NO_FUNCTION									0
#define NO_FAULT									0
#define NO_ERROR                                  	0 

#define ERROR_OVERVOLTAGE							1 // E01 (E06 blinking for XH18)
#define ERROR_TORQUE_SENSOR                       	2 // E02
#define ERROR_CADENCE_SENSOR			          	3 // E03
#define ERROR_MOTOR_BLOCKED                       	4 // E04
#define ERROR_MOTOR_CHECK                       	5 // E05 (E03 blinking for XH18)
#define ERROR_OVERTEMPERATURE						6 // E06
#define ERROR_BATTERY_OVERCURRENT                 	7 // E07 (E04 blinking for XH18)
#define ERROR_SPEED_SENSOR							8 // E08
#define ERROR_WRITE_EEPROM  					  	9 // E09 (E08 blinking for XH18)


// optional ADC function
#if ENABLE_TEMPERATURE_LIMIT && ENABLE_THROTTLE
#define OPTIONAL_ADC_FUNCTION                 		NOT_IN_USE
#elif ENABLE_TEMPERATURE_LIMIT
#define OPTIONAL_ADC_FUNCTION                 		TEMPERATURE_CONTROL
#elif ENABLE_THROTTLE && ENABLE_BRAKE_SENSOR
#define OPTIONAL_ADC_FUNCTION                 		THROTTLE_CONTROL
#else
#define OPTIONAL_ADC_FUNCTION                 		NOT_IN_USE
#endif

// motor inductance & cruise pid parameter
#if MOTOR_TYPE
// 36 volt motor
#define MOTOR_INDUCTANCE_x1048576					80
#define CRUISE_PID_KP                             	14
#define CRUISE_PID_KI                             	0.7

//bemf 36V motor = 0.0806 V/(rad/s) = 0.5 V/(rev/s) = 0.0633 V/erps source:  https://avdweb.nl/solar-bike/hub-motor/efficiency-bldc-motor-tongsheng-tsdz2-and-astro-3205-compared
#define K_BEMF_X1000                               	63U
#else
// 48 volt motor
#define MOTOR_INDUCTANCE_x1048576					142
#define CRUISE_PID_KP                             	12
#define CRUISE_PID_KI                             	1

//casainho said 48V motor has the same max speed (4000rpm) as 36V motor [with corresponding battery] and 4/3 more windings, so the BEMF factor can be scaled by 4/3:
//bemf 48V motor: 0.0633 * 48/36 = 0.0844 V/erps:
#define K_BEMF_X1000                               	84U
#endif

// wheel perimeter
#define WHEEL_PERIMETER_0							(uint8_t) (WHEEL_PERIMETER & 0x00FF)
#define WHEEL_PERIMETER_1							(uint8_t) ((WHEEL_PERIMETER >> 8) & 0x00FF)

// wheel speed parameters
//#define OEM_WHEEL_SPEED_DIVISOR						315 // at 15.625KHz
#define OEM_WHEEL_SPEED_DIVISOR						384 // at 19.047KHz

// BATTERY PARAMETER
// battery low voltage cut off
#define BATTERY_LOW_VOLTAGE_CUT_OFF_X10_0		(uint8_t) ((uint16_t)(BATTERY_LOW_VOLTAGE_CUT_OFF * 10) & 0x00FF)
#define BATTERY_LOW_VOLTAGE_CUT_OFF_X10_1		(uint8_t) (((uint16_t)(BATTERY_LOW_VOLTAGE_CUT_OFF * 10) >> 8) & 0x00FF)
// battery voltage to be subtracted from the cut-off 8bit
#define DIFFERENCE_CUT_OFF_SHUTDOWN_8_BIT			26
// battery voltage for saving battery capacity at shutdown
#define BATTERY_VOLTAGE_SHUTDOWN_8_BIT			(uint8_t) ((uint16_t)(BATTERY_LOW_VOLTAGE_CUT_OFF * 250 / BATTERY_VOLTAGE_PER_10_BIT_ADC_STEP_X1000)) - ((uint16_t) DIFFERENCE_CUT_OFF_SHUTDOWN_8_BIT)
// max battery power div25
#define TARGET_MAX_BATTERY_POWER_DIV25			(uint8_t)(TARGET_MAX_BATTERY_POWER / 25)
// power street limit value div25
#define STREET_MODE_POWER_LIMIT_DIV25           (uint8_t)(STREET_MODE_POWER_LIMIT / 25)
// battery voltage reset SOC percentage
#define BATTERY_VOLTAGE_RESET_SOC_PERCENT_X10   (uint16_t)((float)LI_ION_CELL_RESET_SOC_PERCENT * (float)(BATTERY_CELLS_NUMBER * 10))
// battery SOC eeprom value saved (8 bit)
#define BATTERY_SOC								0
// battery SOC % threshold x10 (volts calc)
#define BATTERY_SOC_PERCENT_THRESHOLD_X10		150

// cell bars
#if ENABLE_VLCD6 || ENABLE_XH18
#define LI_ION_CELL_VOLTS_6_X100		(uint16_t)((float)LI_ION_CELL_OVERVOLT * 100)
#define LI_ION_CELL_VOLTS_5_X100		(uint16_t)((float)LI_ION_CELL_RESET_SOC_PERCENT * 100)
#define LI_ION_CELL_VOLTS_4_X100		(uint16_t)((float)LI_ION_CELL_VOLTS_FULL * 100)
#define LI_ION_CELL_VOLTS_3_X100		(uint16_t)((float)LI_ION_CELL_VOLTS_3_OF_4 * 100)
#define LI_ION_CELL_VOLTS_2_X100		(uint16_t)((float)LI_ION_CELL_VOLTS_2_OF_4 * 100)
#define LI_ION_CELL_VOLTS_1_X100		(uint16_t)((float)LI_ION_CELL_VOLTS_1_OF_4 * 100)
#define LI_ION_CELL_VOLTS_0_X100		(uint16_t)((float)LI_ION_CELL_VOLTS_EMPTY * 100)
#else // ENABLE_VLCD5 or 850C
#define LI_ION_CELL_VOLTS_8_X100		(uint16_t)((float)LI_ION_CELL_OVERVOLT * 100)
#define LI_ION_CELL_VOLTS_7_X100		(uint16_t)((float)LI_ION_CELL_RESET_SOC_PERCENT * 100)
#define LI_ION_CELL_VOLTS_6_X100		(uint16_t)((float)LI_ION_CELL_VOLTS_FULL * 100)
#define LI_ION_CELL_VOLTS_5_X100		(uint16_t)((float)LI_ION_CELL_VOLTS_5_OF_6 * 100)
#define LI_ION_CELL_VOLTS_4_X100		(uint16_t)((float)LI_ION_CELL_VOLTS_4_OF_6 * 100)
#define LI_ION_CELL_VOLTS_3_X100		(uint16_t)((float)LI_ION_CELL_VOLTS_3_OF_6 * 100)
#define LI_ION_CELL_VOLTS_2_X100		(uint16_t)((float)LI_ION_CELL_VOLTS_2_OF_6 * 100)
#define LI_ION_CELL_VOLTS_1_X100		(uint16_t)((float)LI_ION_CELL_VOLTS_1_OF_6 * 100)
#define LI_ION_CELL_VOLTS_0_X100		(uint16_t)((float)LI_ION_CELL_VOLTS_EMPTY * 100)
#endif

// assist level 0
#define TORQUE_ASSIST_LEVEL_0        0
#define CADENCE_ASSIST_LEVEL_0       0
#define EMTB_ASSIST_LEVEL_0          0
#define WALK_ASSIST_LEVEL_0          0
#define CRUISE_TARGET_SPEED_LEVEL_0  0

// power assist level
#define POWER_ASSIST_LEVEL_OFF       0
#define POWER_ASSIST_LEVEL_ECO       (uint8_t)(POWER_ASSIST_LEVEL_1 / 2)
#define POWER_ASSIST_LEVEL_TOUR      (uint8_t)(POWER_ASSIST_LEVEL_2 / 2)
#define POWER_ASSIST_LEVEL_SPORT     (uint8_t)(POWER_ASSIST_LEVEL_3 / 2)
#define POWER_ASSIST_LEVEL_TURBO     (uint8_t)(POWER_ASSIST_LEVEL_4 / 2)

// walk assist
//#define WALK_ASSIST_THRESHOLD_SPEED_X10	(uint8_t)(WALK_ASSIST_THRESHOLD_SPEED * 10)
#define WALK_ASSIST_THRESHOLD_SPEED_X10	(uint8_t)(WALK_ASSIST_THRESHOLD_SPEED)
#define WALK_ASSIST_ERPS_THRESHOLD				20
#define WALK_ASSIST_ADJ_DELAY_MIN				4
#define WALK_ASSIST_ADJ_DELAY_STARTUP			10
#define WALK_ASSIST_DUTY_CYCLE_MIN              40
#define WALK_ASSIST_DUTY_CYCLE_STARTUP			50
#define WALK_ASSIST_DUTY_CYCLE_MAX              130
#define WALK_ASSIST_ADC_BATTERY_CURRENT_MAX     40

// startup assist
#define STARTUP_ASSIST_MAX_TIME					100 // 10.0 sec

// cruise threshold (speed limit min km/h x10)
#define CRUISE_THRESHOLD_SPEED_X10		(uint8_t)(CRUISE_THRESHOLD_SPEED * 10)
#define CRUISE_THRESHOLD_SPEED_X10_DEFAULT		80

// odometer compensation for displayed data (eeprom)
#define ODOMETER_COMPENSATION					0
// zero odometer compensation
#define ZERO_ODOMETER_COMPENSATION				100000000

#define ASSISTANCE_WITH_ERROR_ENABLED			0

#define AVAIABLE_FOR_FUTURE_USE					0 // EEPROM

#endif // _MAIN_H_
