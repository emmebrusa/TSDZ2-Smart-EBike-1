/*
 * TongSheng TSDZ2 motor controller firmware/
 *
 * Copyright (C) Casainho, Leon, MSpider65 2020.
 *
 * Released under the GPL License, Version 3
 */

#include <stdint.h>
#include <math.h>
#include "motor.h"
#include "interrupts.h"
#include "stm8s_gpio.h"
#include "stm8s_tim1.h"
#include "ebike_app.h"
#include "pins.h"
#include "pwm.h"
#include "adc.h"
#include "uart.h"
#include "watchdog.h"
#include "common.h"
#include "eeprom.h"

#define SVM_TABLE_LEN   256
#define SIN_TABLE_LEN   59

uint8_t ui8_svm_table[SVM_TABLE_LEN] = {
        199,
        200,
        202,
        203,
        204,
        205,
        206,
        207,
        208,
        208,
        209,
        210,
        210,
        211,
        211,
        211,
        212,
        212,
        212,
        212,
        212,
        212,
        212,
        212,
        211,
        211,
        211,
        210,
        210,
        209,
        208,
        208,
        207,
        206,
        206,
        205,
        204,
        203,
        202,
        201,
        200,
        199,
        196,
        192,
        188,
        184,
        180,
        176,
        172,
        167,
        163,
        159,
        154,
        150,
        146,
        141,
        137,
        133,
        128,
        124,
        119,
        115,
        110,
        106,
        102,
        97,
        93,
        88,
        84,
        79,
        75,
        71,
        66,
        62,
        58,
        53,
        49,
        45,
        40,
        36,
        32,
        28,
        24,
        20,
        16,
        13,
        12,
        11,
        10,
        9,
        8,
        7,
        6,
        6,
        5,
        4,
        4,
        3,
        2,
        2,
        1,
        1,
        1,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        1,
        1,
        1,
        2,
        2,
        3,
        4,
        4,
        5,
        6,
        7,
        8,
        9,
        10,
        12,
        13,
        14,
        13,
        12,
        10,
        9,
        8,
        7,
        6,
        5,
        4,
        4,
        3,
        2,
        2,
        1,
        1,
        1,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        1,
        1,
        1,
        2,
        2,
        3,
        4,
        4,
        5,
        6,
        6,
        7,
        8,
        9,
        10,
        11,
        12,
        13,
        16,
        20,
        24,
        28,
        32,
        36,
        40,
        45,
        49,
        53,
        58,
        62,
        66,
        71,
        75,
        79,
        84,
        88,
        93,
        97,
        102,
        106,
        110,
        115,
        119,
        124,
        128,
        133,
        137,
        141,
        146,
        150,
        154,
        159,
        163,
        167,
        172,
        176,
        180,
        184,
        188,
        192,
        196,
        199,
        200,
        201,
        202,
        203,
        204,
        205,
        206,
        206,
        207,
        208,
        208,
        209,
        210,
        210,
        211,
        211,
        211,
        212,
        212,
        212,
        212,
        212,
        212,
        212,
        212,
        211,
        211,
        211,
        210,
        210,
        209,
        208,
        208,
        207,
        206,
        205,
        204,
        203,
        202,
        200,
        199,
        198
};

uint8_t ui8_sin_table[SIN_TABLE_LEN] = { 3, 6, 9, 12, 16, 19, 22, 25, 28, 31, 34, 37, 40, 43, 46, 49, 52, 54, 57, 60,
        63, 66, 68, 71, 73, 76, 78, 81, 83, 86, 88, 90, 92, 95, 97, 99, 101, 102, 104, 106, 108, 109, 111, 113, 114,
        115, 117, 118, 119, 120, 121, 122, 123, 124, 125, 125, 126, 126, 127 };

// motor variables
uint8_t ui8_pwm_counter_valid_a = 0;
uint16_t ui16_PWM_cycles_counter_a = 3;
uint8_t ui8_pwm_counter_valid_b = 0;
uint16_t ui16_PWM_cycles_counter_b = 3;
uint16_t ui16_PWM_cycles_counter_6 = 3;
uint16_t ui16_PWM_cycles_counter_total = 0xffff;
uint8_t ui8_motor_commutation_type = BLOCK_COMMUTATION;
volatile uint16_t ui16_motor_speed_erps = 0;
static uint8_t ui8_motor_rotor_absolute_angle;


// power variables
volatile uint8_t ui8_controller_duty_cycle_ramp_up_inverse_step = PWM_DUTY_CYCLE_RAMP_UP_INVERSE_STEP_DEFAULT;
volatile uint8_t ui8_controller_duty_cycle_ramp_down_inverse_step = PWM_DUTY_CYCLE_RAMP_DOWN_INVERSE_STEP_DEFAULT;
volatile uint16_t ui16_adc_battery_voltage_filtered = 0;
volatile uint8_t ui8_adc_battery_voltage_cut_off = 0xff;
volatile uint8_t ui8_adc_battery_current_filtered = 0;
volatile uint8_t ui8_controller_adc_battery_current = 0;
volatile uint8_t ui8_controller_adc_battery_current_target = 0;
volatile uint8_t ui8_g_duty_cycle = 0;
volatile uint8_t ui8_fw_angle = 0;
volatile uint8_t ui8_fw_angle_max;
volatile uint8_t ui8_controller_duty_cycle_target = 0;
volatile uint8_t ui8_g_foc_angle = 0;
volatile uint8_t ui8_g_assist_level = 0;
static uint8_t ui8_counter_duty_cycle_ramp_up = 0;
static uint8_t ui8_counter_duty_cycle_ramp_down = 0;

// battery current variables
static uint8_t ui8_adc_battery_current_acc = 0;
static uint8_t ui8_adc_motor_phase_current;

// brakes
volatile uint8_t ui8_brake_state = 0;

// cadence sensor
#define NO_PAS_REF 5
volatile uint16_t ui16_cadence_sensor_ticks = 0;
//volatile uint32_t ui32_crank_revolutions_x20 = 0;
static uint16_t ui16_cadence_sensor_ticks_counter_min = CADENCE_SENSOR_CALC_COUNTER_MIN;
static uint8_t ui8_pas_state_old = 4;
static uint16_t ui16_cadence_calc_counter, ui16_cadence_stop_counter;
static uint8_t ui8_cadence_calc_ref_state = NO_PAS_REF;
const static uint8_t ui8_pas_old_valid_state[4] = { 0x01, 0x03, 0x00, 0x02 };

// wheel speed sensor
volatile uint16_t ui16_wheel_speed_sensor_ticks = 0;
volatile uint16_t ui16_wheel_speed_sensor_ticks_counter_min = 0;
volatile uint32_t ui32_wheel_speed_sensor_ticks_total = 0;

// battery soc
static uint8_t ui8_battery_SOC_saved_flag = 0;
volatile uint8_t ui8_battery_SOC_reset_flag = 0;

// ebike loop check
volatile uint16_t ui16_ebike_check_counter = 0;
#define EBIKE_CHECK_COUNTER_THRESHOLD	1000

// torque sensor check
static uint16_t ui16_adc_torque_sensor_check = 0;
static uint16_t ui16_motor_check_counter = 0;
#define MOTOR_CHECK_COUNTER_THRESHOLD	40000 // 2 sec

// system functions
void read_battery_voltage(void);
void calc_foc_angle(void);
uint8_t asin_table(uint8_t ui8_inverted_angle_x128);

void motor_controller(void) {
    ui16_motor_speed_erps = ((uint16_t) DOUBLE_PWM_CYCLES_SECOND) / (ui16_PWM_cycles_counter_total);
    read_battery_voltage();
    calc_foc_angle();
}

// Measures did with a 24V Q85 328 RPM motor, rotating motor backwards by hand:
// Hall sensor A positivie to negative transition | BEMF phase B at max value / top of sinewave
// Hall sensor B positivie to negative transition | BEMF phase A at max value / top of sinewave
// Hall sensor C positive to negative transition | BEMF phase C at max value / top of sinewave


// PWM cycle interrupt
// TIM1 is center aligned and every cycle counts up from 0 to 400 and then down from 400 to 0 (25+25us = 50us total time)
// The interrupt fires two times every cycle (when reaches 200 up and down)
// Both interrupts are used to update rotor position counters (max 25us rotor position offset error)
// Up interrupt is used for:
//  - read and filter battery current
//  - start ADC scan conversion
//  - PAS sensor read and cadence computation
//  - Wheel speed sensor read and wheel speed computation
// Down interrupt is used for:
//  - interpolate rotor position
//  - check brake (coaster brake and brake input signal)
//  - calculate and apply duty cycle to TIM1

void TIM1_CAP_COM_IRQHandler(void) __interrupt(TIM1_CAP_COM_IRQHANDLER)
{
	struct_configuration_variables *p_configuration_variables;
    p_configuration_variables = get_configuration_variables();
	
	// save percentage remaining battery capacity at shutdown
	if((UI8_ADC_BATTERY_VOLTAGE < BATTERY_VOLTAGE_SHUTDOWN_8_BIT)
		&&(!ui8_battery_SOC_saved_flag)
		&&(ui8_battery_SOC_reset_flag))
	{
		//disableInterrupts();
		// disable pwm at shutdown
		ui8_motor_enabled = 0;
		motor_disable_pwm();
			
		// unlock memory
		FLASH_Unlock(FLASH_MEMTYPE_DATA);
  
		// wait until data EEPROM area unlocked flag is set
		while (FLASH_GetFlagStatus(FLASH_FLAG_DUL) == RESET) {}

		// write percentage remaining battery capacity x10 8bit to EEPROM
		FLASH_ProgramByte(ADDRESS_BATTERY_SOC, p_configuration_variables->ui8_battery_SOC_percentage_8b);
            
		// wait until end of programming (write or erase operation) flag is set
		while (FLASH_GetFlagStatus(FLASH_FLAG_EOP) == RESET) {}

		// lock memory
		FLASH_Lock(FLASH_MEMTYPE_DATA);
			
		//enableInterrupts();
			
		// battery SOC saved
		ui8_battery_SOC_saved_flag = 1;
	}
	
    // TIM1->CR1 & 0x10 contains counter direction (0=up, 1=down)
    uint8_t ui8_pwm_down;
    if (TIM1->CR1 & 0x10) {
        // trigger ADC conversion (scan conversion, buffered)
        // ADC battery current conversion will start after 3 (ADC prescaler) * 14 (ADC clocks/conversion) * 5 (Channel) = 210 CPU Cycles
        // in the exact middle of PWM cycle
        ADC1->CSR = 0x07;               // clear EOC flag and ADC scan up to channel 7
        ADC1->CR1 |= ADC1_CR1_ADON;     // start ADC1 conversion
        ui8_pwm_down = 1;
    } else
        ui8_pwm_down = 0;

    /****************************************************************************/

    // read hall sensor signals and:
    // - find the motor rotor absolute angle
    // - calc motor speed in erps (ui16_motor_speed_erps)
    // - check so that motor is not rotating backwards, if it does, set ERPS to 0
    static uint8_t ui8_hall_sensors_state_last;
    uint8_t ui8_hall_sensors_state;

    // read hall sensors signal pins and mask other pins
    // hall sensors sequence with motor forward rotation: C, CB, B, BA, A, AC, ..
    // ui8_hall_sensors_state:
    //      bit 5 0x20 Hall sensor A
    //      bit 4 0x10 Hall sensor C
    //      bit 2 0x04 Hall sensor B
    // ui8_hall_sensors_state sequence with motor forward rotation: 0x10, 0x14, 0x04, 0x24, 0x20, 0x30
    //
    // Check PWMCalculations.xlsx in the repositoy about ui16_PWM_cycles_counter and
    // ui16_PWM_cycles_counter_6 initialization values.

    ui8_hall_sensors_state = (HALL_SENSOR_A__PORT->IDR & HALL_SENSOR_A__PIN)
            | (HALL_SENSOR_B__PORT->IDR & HALL_SENSOR_B__PIN)
            | ((HALL_SENSOR_C__PORT->IDR & HALL_SENSOR_C__PIN) >> 1);


    // run next code only when the hall sensors signal changes
    if (ui8_hall_sensors_state != ui8_hall_sensors_state_last) {
        // check first states with heavier computation
        if (ui8_hall_sensors_state == 0x20) {
            // check half ERPS flag and motor rotational direction
            if (ui8_pwm_counter_valid_a && (ui8_hall_sensors_state_last == 0x24)) {
                ui16_PWM_cycles_counter_total = ui16_PWM_cycles_counter_a;

                // update motor commutation state based on motor speed
                if (ui16_PWM_cycles_counter_total < (DOUBLE_PWM_CYCLES_SECOND / MOTOR_ROTOR_ERPS_START_INTERPOLATION_60_DEGREES)) {
                    ui8_motor_commutation_type = SINEWAVE_INTERPOLATION_60_DEGREES;
                } else {
                    ui8_motor_commutation_type = BLOCK_COMMUTATION;
                    ui8_g_foc_angle = 0;
                }

                ui16_PWM_cycles_counter_a = 0;
            }
            ui8_pwm_counter_valid_a = 1;
            ui8_motor_rotor_absolute_angle = (uint8_t) MOTOR_ROTOR_ANGLE_210;
        } else if (ui8_hall_sensors_state == 0x14) {
            // BEMF is always 90 degrees advanced over motor rotor position degree zero
            // and here (hall sensor C blue wire, signal transition from positive to negative),
            // phase B BEMF is at max value (measured on osciloscope by rotating the motor)
            // check half ERPS flag and motor rotational direction
            if (ui8_pwm_counter_valid_b && (ui8_hall_sensors_state_last == 0x10)) {
                ui16_PWM_cycles_counter_total = ui16_PWM_cycles_counter_b;

                // update motor commutation state based on motor speed
                if (ui16_PWM_cycles_counter_total < (DOUBLE_PWM_CYCLES_SECOND / MOTOR_ROTOR_ERPS_START_INTERPOLATION_60_DEGREES)) {
                    ui8_motor_commutation_type = SINEWAVE_INTERPOLATION_60_DEGREES;
                } else {
                    ui8_motor_commutation_type = BLOCK_COMMUTATION;
                    ui8_g_foc_angle = 0;
                }

                ui16_PWM_cycles_counter_b = 0;
            }
            ui8_pwm_counter_valid_b = 1;
            ui8_motor_rotor_absolute_angle = (uint8_t) MOTOR_ROTOR_ANGLE_30;
        } else {
            switch (ui8_hall_sensors_state) {
                case 0x30:
                    ui8_motor_rotor_absolute_angle = (uint8_t) MOTOR_ROTOR_ANGLE_270;
                    break;

                case 0x10:
                    ui8_motor_rotor_absolute_angle = (uint8_t) MOTOR_ROTOR_ANGLE_330;
                    break;

                case 0x04:
                    ui8_motor_rotor_absolute_angle = (uint8_t) MOTOR_ROTOR_ANGLE_90;
                    break;

                case 0x24:
                    ui8_motor_rotor_absolute_angle = (uint8_t) MOTOR_ROTOR_ANGLE_150;
                    break;

                default:
                    return;
            }
        }

        ui16_PWM_cycles_counter_6 = 2;

        // update last hall sensor state
        ui8_hall_sensors_state_last = ui8_hall_sensors_state;
    }

    /****************************************************************************/

    // count number of fast loops / PWM cycles and reset some states when motor is near zero speed
    if (ui16_PWM_cycles_counter_a < PWM_CYCLES_COUNTER_MAX) {
        ui16_PWM_cycles_counter_a++;
        ui16_PWM_cycles_counter_b++;
        ui16_PWM_cycles_counter_6++;
    } else {
        // motor is stopped or near zero speed
        ui16_PWM_cycles_counter_a = 0;
        ui16_PWM_cycles_counter_b = 0;
        ui16_PWM_cycles_counter_6 = 2;
        ui8_pwm_counter_valid_a = 0;
        ui8_pwm_counter_valid_b = 0;
        ui16_PWM_cycles_counter_total = 0xffff;
        ui8_g_foc_angle = 0;
        ui8_motor_commutation_type = BLOCK_COMMUTATION;
        ui8_hall_sensors_state_last = 0; // this way we force execution of hall sensors code next time
    }

    // TIM1->CR1 & 0x10 contains counter direction (0=up, 1=down)
    if (ui8_pwm_down) {
        uint8_t ui8_svm_table_index;
        uint8_t ui8_phase_a_voltage;
        uint8_t ui8_phase_b_voltage;
        uint8_t ui8_phase_c_voltage;
        uint8_t ui8_temp;
        uint16_t ui16_value;

        /****************************************************************************/

        // - calculate interpolation angle and sine wave table index
    #define DO_INTERPOLATION 1 // may be useful to disable interpolation when debugging
    #if DO_INTERPOLATION == 1
        // calculate the interpolation angle (and it doesn't work when motor starts and at very low speeds)
        if (ui8_motor_commutation_type != BLOCK_COMMUTATION) {
            // division by 0: ui16_PWM_cycles_counter_total should never be 0
            // TODO: verifiy if (ui16_PWM_cycles_counter_6 << 8) do not overflow
            uint8_t ui8_interpolation_angle = (ui16_PWM_cycles_counter_6 << 8) / ui16_PWM_cycles_counter_total;
            uint8_t ui8_motor_rotor_angle = ui8_motor_rotor_absolute_angle + ui8_interpolation_angle;
            // we need to put phase voltage 90 degrees ahead of rotor position, to get current 90 degrees ahead and have max torque per amp
            ui8_svm_table_index = ui8_motor_rotor_angle + ui8_g_foc_angle - 63;
        } else
    #endif
        {
            // we need to put phase voltage 90 degrees ahead of rotor position, to get current 90 degrees ahead and have max torque per amp
            ui8_svm_table_index = ui8_motor_rotor_absolute_angle + ui8_g_foc_angle - 63;
        }

        /****************************************************************************/

        // brakes (consider in slower loop)
        // - check if brakes are installed and enabled
        // - check if coaster brake is engaged
        // - check if brakes are engaged
        // check if brakes are installed and enabled

		#if COASTER_BRAKE_ENABLED
        // check if coaster brake is engaged
        if (UI16_ADC_10_BIT_TORQUE_SENSOR < ui16_adc_coaster_brake_threshold) {
            // set brake state
            ui8_brake_state = 1;
        } else {
            // set brake state
            //ui8_brake_state = ((BRAKE__PORT->IDR & BRAKE__PIN) ^ BRAKE__PIN);
			ui8_brake_state = ((BRAKE__PORT->IDR & (uint8_t)BRAKE__PIN) == 0);
        }
		#else
		// set brake state
        //ui8_brake_state = ((BRAKE__PORT->IDR & BRAKE__PIN) ^ BRAKE__PIN);
		ui8_brake_state = ((BRAKE__PORT->IDR & (uint8_t)BRAKE__PIN) == 0);
		#endif

        /****************************************************************************/
		
		// ebike_loop check
		if(ui16_ebike_check_counter > EBIKE_CHECK_COUNTER_THRESHOLD)
			ui8_brake_state = 1;
		else
			ui16_ebike_check_counter++;
	
		if(!p_configuration_variables->ui8_assist_with_error_enabled) {
			// check if the motor goes alone with
			// torque sensor adc value < torque sensor offset
			// or battery current target = 0
			ui16_adc_torque_sensor_check = UI16_ADC_10_BIT_TORQUE_SENSOR;
			
			if((ui16_adc_torque_sensor_check < ui16_adc_pedal_torque_offset_cal)||(!ui8_adc_battery_current_target)) {
				if((ui16_motor_speed_erps)
				&&(!ui16_cadence_sensor_ticks)
				#if OPTIONAL_ADC_FUNCTION == THROTTLE_CONTROL
				&&(UI8_ADC_THROTTLE < ADC_THROTTLE_MIN_VALUE)
				#endif
				&&(ui8_riding_torque_mode)) {
					ui16_motor_check_counter++;
				}
				else {
					ui16_motor_check_counter = 0;
				}
			}
			else {
				ui16_motor_check_counter = 0;
			}
			
			if(ui16_motor_check_counter > MOTOR_CHECK_COUNTER_THRESHOLD) {
				ui8_brake_state = 1;
				ui8_system_state = ERROR_MOTOR_CHECK;
			}
		}
		
        /****************************************************************************/
        // PWM duty_cycle controller:
        // - limit battery undervolt
        // - limit battery max current
        // - limit motor max phase current
        // - limit motor max ERPS
        // - ramp up/down PWM duty_cycle and/or field weakening angle value

        // check if to decrease, increase or maintain duty cycle
        if ((ui8_g_duty_cycle > ui8_controller_duty_cycle_target)
                || (ui8_adc_battery_current_filtered > ui8_controller_adc_battery_current_target)
                || (ui8_adc_motor_phase_current > ui8_adc_motor_phase_current_max)
                || (ui16_PWM_cycles_counter_total < (DOUBLE_PWM_CYCLES_SECOND / MOTOR_OVER_SPEED_ERPS))
                || (UI8_ADC_BATTERY_VOLTAGE < ui8_adc_battery_voltage_cut_off)
                || (ui8_fw_angle > ui8_fw_angle_max)
                || (ui8_brake_state)
				|| (!ui8_g_assist_level)) {
			
            // reset duty cycle ramp up counter (filter)
            ui8_counter_duty_cycle_ramp_up = 0;
			
			// motor fast stop
			if(ui8_pedal_cadence_fast_stop) {
				ui8_controller_duty_cycle_ramp_down_inverse_step = PWM_DUTY_CYCLE_RAMP_DOWN_INVERSE_STEP_MIN;
			}
			
            // ramp down duty cycle
            if (++ui8_counter_duty_cycle_ramp_down >= ui8_controller_duty_cycle_ramp_down_inverse_step) {
                ui8_counter_duty_cycle_ramp_down = 0;
                // decrement field weakening angle if set or duty cycle if not
                if (ui8_fw_angle > 0)
                    ui8_fw_angle--;
                else if (ui8_g_duty_cycle > 0)
                    ui8_g_duty_cycle--;
            }
        } else if (ui8_g_duty_cycle < ui8_controller_duty_cycle_target) {
            // reset duty cycle ramp down counter (filter)
            ui8_counter_duty_cycle_ramp_down = 0;

            // ramp up duty cycle
            if (++ui8_counter_duty_cycle_ramp_up >= ui8_controller_duty_cycle_ramp_up_inverse_step) {
                ui8_counter_duty_cycle_ramp_up = 0;

                // increment duty cycle
                if (ui8_g_duty_cycle < PWM_DUTY_CYCLE_MAX) {
                    ui8_g_duty_cycle++;
                }
            }
		#if FIELD_WEAKENING_ENABLED
        } else if ((ui8_g_duty_cycle == PWM_DUTY_CYCLE_MAX)
				&& (ui16_motor_speed_erps > MOTOR_SPEED_FIELD_WEAKENING_MIN) // do not enable at low motor speed / low cadence
                && (ui8_fw_angle_max > 0)
				&& (ui8_adc_battery_current_filtered < ui8_controller_adc_battery_current_target)) {
            // reset duty cycle ramp down counter (filter)
            ui8_counter_duty_cycle_ramp_down = 0;

            if (++ui8_counter_duty_cycle_ramp_up > ui8_controller_duty_cycle_ramp_up_inverse_step) {
               ui8_counter_duty_cycle_ramp_up = 0;

               // increment field weakening angle
               if (ui8_fw_angle < ui8_fw_angle_max)
                   ui8_fw_angle++;
            }
		#endif
        } else {
            // duty cycle is where it needs to be so reset ramp counters (filter)
            ui8_counter_duty_cycle_ramp_up = 0;
            ui8_counter_duty_cycle_ramp_down = 0;
        }

        ui8_svm_table_index += ui8_fw_angle;

        /****************************************************************************/

        // calculate final PWM duty_cycle values to be applied to TIMER1

        // scale and apply PWM duty_cycle for the 3 phases
        // phase A is advanced 240 degrees over phase B
        // Max of SVM table is 202 and ui8_tmp goes from 0 to 100 (101*255/256) and
        // ui8_phase_x_voltage goes from 0 (MIDDLE_PWM_COUNTER - ui8_temp) to 200 (MIDDLE_PWM_COUNTER + ui8_temp)

        ui8_temp = ui8_svm_table[(uint8_t) (ui8_svm_table_index + 171)]; /* +240� */
        if (ui8_temp > MIDDLE_SVM_TABLE) {
            ui16_value = (uint16_t)((uint8_t)(ui8_temp - MIDDLE_SVM_TABLE) * (uint8_t)ui8_g_duty_cycle);
            ui8_temp = (uint8_t) (ui16_value >> PWM_DUTY_CYCLE_BITS);
            ui8_phase_a_voltage = MIDDLE_PWM_COUNTER + ui8_temp;
        } else {
            ui16_value = (uint16_t)((uint8_t)(MIDDLE_SVM_TABLE - ui8_temp) * (uint8_t)ui8_g_duty_cycle);
            ui8_temp = (uint8_t) (ui16_value >> PWM_DUTY_CYCLE_BITS);
            ui8_phase_a_voltage = MIDDLE_PWM_COUNTER - ui8_temp;
        }

        // phase B as reference phase
        ui8_temp = ui8_svm_table[ui8_svm_table_index];
        if (ui8_temp > MIDDLE_SVM_TABLE) {
            ui16_value = (uint16_t) ((uint8_t)(ui8_temp - MIDDLE_SVM_TABLE) * (uint8_t)ui8_g_duty_cycle);
            ui8_temp = (uint8_t) (ui16_value >> PWM_DUTY_CYCLE_BITS);
            ui8_phase_b_voltage = MIDDLE_PWM_COUNTER + ui8_temp;
        } else {
            ui16_value = (uint16_t) ((uint8_t)(MIDDLE_SVM_TABLE - ui8_temp) * (uint8_t)ui8_g_duty_cycle);
            ui8_temp = (uint8_t) (ui16_value >> PWM_DUTY_CYCLE_BITS);
            ui8_phase_b_voltage = MIDDLE_PWM_COUNTER - ui8_temp;
        }

        // phase C is advanced 120 degrees over phase B
        ui8_temp = ui8_svm_table[(uint8_t) (ui8_svm_table_index + 85 /* 120º */)];
        if (ui8_temp > MIDDLE_SVM_TABLE) {
            ui16_value = (uint16_t) ((uint8_t)(ui8_temp - MIDDLE_SVM_TABLE) * (uint8_t)ui8_g_duty_cycle);
            ui8_temp = (uint8_t) (ui16_value >> PWM_DUTY_CYCLE_BITS);
            ui8_phase_c_voltage = MIDDLE_PWM_COUNTER + ui8_temp;
        } else {
            ui16_value = (uint16_t) ((uint8_t)(MIDDLE_SVM_TABLE - ui8_temp) * (uint8_t)ui8_g_duty_cycle);
            ui8_temp = (uint8_t) (ui16_value >> PWM_DUTY_CYCLE_BITS);
            ui8_phase_c_voltage = MIDDLE_PWM_COUNTER - ui8_temp;
        }

        // set final duty_cycle value
        // phase B
        TIM1->CCR3H = (uint8_t) (ui8_phase_b_voltage >> 7);
        TIM1->CCR3L = (uint8_t) (ui8_phase_b_voltage << 1);
        // phase C
        TIM1->CCR2H = (uint8_t) (ui8_phase_c_voltage >> 7);
        TIM1->CCR2L = (uint8_t) (ui8_phase_c_voltage << 1);
        // phase A
        TIM1->CCR1H = (uint8_t) (ui8_phase_a_voltage >> 7);
        TIM1->CCR1L = (uint8_t) (ui8_phase_a_voltage << 1);

    } else {
        /****************************************************************************/

        // Calculate Field Weakening max Angle based on current motor speed
        // Field weakening max angle is 0 when cadence is below 48rpm
        // Field weakening max angle is 6 when cadence is above 105rmp
        // between 48 and 105rpm  goes from 1 to 5
        #define FW_COUNTER_MIN  65U
        #define FW_DIVISOR      16U
        #define FW_COUNTER_MAX (FW_COUNTER_MIN + (FW_DIVISOR*5))

        if (ui16_PWM_cycles_counter_total >= FW_COUNTER_MAX)
            ui8_fw_angle_max = 0;
        else if ((uint8_t)ui16_PWM_cycles_counter_total <= FW_COUNTER_MIN)
            ui8_fw_angle_max = 6;
        else
            ui8_fw_angle_max = 5 - ((uint8_t)((uint8_t)ui16_PWM_cycles_counter_total - FW_COUNTER_MIN) / (uint8_t)FW_DIVISOR);


        /****************************************************************************/
        /*
        // Read battery current
        // Left alignment: Read MSB first then read LSB !
        ui8_temp = ADC1->DB5RH;
        ui8_temp <<= 2;
        ui8_temp |= ADC1->DB5RL
        ui8_adc_battery_current_acc >>= 1;
        ui8_adc_battery_current_filtered >>= 1;
        ui8_adc_battery_current_acc = (uint8_t)(ui8_temp >> 1) + ui8_adc_battery_current_acc;
        ui8_adc_battery_current_filtered = (uint8_t)(ui8_adc_battery_current_acc >> 1) + ui8_adc_battery_current_filtered;

        // calculate motor phase current ADC value
        if (ui8_g_duty_cycle > 0)
            ui8_adc_motor_phase_current = (uint16_t)((uint16_t)((uint16_t)ui8_adc_battery_current_filtered << 6)) / ui8_g_duty_cycle;
        else
            ui8_adc_motor_phase_current = 0;
        */

        #ifndef __CDT_PARSER__ // avoid Eclipse syntax check
        __asm
        ld  a, 0x53EA                               // ui8_temp = ADC1->DB5RH;
        sll a                                       // ui8_temp <<= 2;
        sll a
        or  a, 0x53EB                               // ui8_temp |= ADC1->DB5RL;
        srl _ui8_adc_battery_current_acc+0          // ui8_adc_battery_current_acc >>= 1;
        srl a                                       // ui8_adc_battery_current_acc = (uint8_t)(ui8_temp >> 1) + ui8_adc_battery_current_acc;
        add a, _ui8_adc_battery_current_acc+0
        ld  _ui8_adc_battery_current_acc+0, a
        srl _ui8_adc_battery_current_filtered+0     // ui8_adc_battery_current_filtered >>= 1;
        srl a                                       // ui8_adc_battery_current_filtered = (uint8_t)(ui8_adc_battery_current_acc >> 1) + ui8_adc_battery_current_filtered;
        add a, _ui8_adc_battery_current_filtered+0
        ld  _ui8_adc_battery_current_filtered+0, a

        tnz _ui8_g_duty_cycle+0                     // if (ui8_g_duty_cycle > 0)
        jreq 00001$
        clrw    x          // ui8_adc_motor_phase_current = (ui8_adc_battery_current_filtered << 6)) / ui8_g_duty_cycle;
        ld  xh, a
        srlw    x
        srlw    x
        ld  a, _ui8_g_duty_cycle+0
        div    x, a
        ld  a, xl
        ld  _ui8_adc_motor_phase_current+0, a
        jra 00002$
        00001$:
        clr _ui8_adc_motor_phase_current+0      // ui8_adc_motor_phase_current = 0;
        00002$:
        __endasm;
        #endif

        /****************************************************************************/

        /*
         * - New pedal start/stop detection Algorithm (by MSpider65) -
         *
         * Pedal start/stop detection uses both transitions of both PAS sensors
         * ui8_pas_state stores the PAS1 and PAS2 state: bit0=PAS1,  bit1=PAS2
         * Pedal forward ui8_pas_state sequence is: 0x01 -> 0x00 -> 0x02 -> 0x03 -> 0x01
         * After a stop, the first forward transition is taken as reference transition
         * Following forward transition sets the cadence to 7RPM for immediate startup
         * Then, starting form the second reference transition, the cadence is calculated based on counter value
         * All transitions resets the stop detection counter (much faster stop detection):
         */

        uint8_t ui8_pas_state = (PAS1__PORT->IDR & PAS1__PIN) | ((PAS2__PORT->IDR & PAS2__PIN) >> 6);

        if (ui8_pas_state != ui8_pas_state_old) {
            if (ui8_pas_state_old != ui8_pas_old_valid_state[ui8_pas_state]) {
                // wrong state sequence: backward rotation
                ui16_cadence_sensor_ticks = 0;
                ui8_cadence_calc_ref_state = NO_PAS_REF;
                goto skip_cadence;
            }

			// motor fast stop
			if(ui8_pedal_cadence_fast_stop && ui16_cadence_sensor_ticks) {
				ui16_cadence_sensor_ticks_counter_min = ui16_cadence_sensor_ticks - (CADENCE_SENSOR_STANDARD_MODE_SCHMITT_TRIGGER_THRESHOLD >> 1);
			}
			else {
				ui16_cadence_sensor_ticks_counter_min = ui16_cadence_ticks_count_min_speed_adj;
			}
			
            // Reference state for crank revolution counter increment
            //if (ui8_pas_state == 0)
            //    ui32_crank_revolutions_x20++;

            if (ui8_pas_state == ui8_cadence_calc_ref_state) {
                // ui16_cadence_calc_counter is valid for cadence calculation
                ui16_cadence_sensor_ticks = ui16_cadence_calc_counter;
                ui16_cadence_calc_counter = 0;
                // software based Schmitt trigger to stop motor jitter when at resolution limits
                ui16_cadence_sensor_ticks_counter_min += CADENCE_SENSOR_STANDARD_MODE_SCHMITT_TRIGGER_THRESHOLD;
            } else if (ui8_cadence_calc_ref_state == NO_PAS_REF) {
                // this is the new reference state for cadence calculation
                ui8_cadence_calc_ref_state = ui8_pas_state;
                ui16_cadence_calc_counter = 0;
            } else if (ui16_cadence_sensor_ticks == 0) {
                // Waiting the second reference transition: set the cadence to 7 RPM for immediate start
                ui16_cadence_sensor_ticks = CADENCE_TICKS_STARTUP;
            }

            skip_cadence:
            // reset the counter used to detect pedal stop
            ui16_cadence_stop_counter = 0;
            // save current PAS state
            ui8_pas_state_old = ui8_pas_state;
        }

        if (++ui16_cadence_stop_counter > ui16_cadence_sensor_ticks_counter_min) {
            // pedals stop detected
            ui16_cadence_sensor_ticks = 0;
            ui16_cadence_stop_counter = 0;
            ui8_cadence_calc_ref_state = NO_PAS_REF;
        } else if (ui8_cadence_calc_ref_state != NO_PAS_REF) {
            // increment cadence tick counter
            ++ui16_cadence_calc_counter;
        }

        /****************************************************************************/

        static uint16_t ui16_wheel_speed_sensor_ticks_counter;
        static uint8_t ui8_wheel_speed_sensor_ticks_counter_started;
        static uint8_t ui8_wheel_speed_sensor_pin_state_old;

        // check wheel speed sensor pin state
        uint8_t ui8_wheel_speed_sensor_pin_state = WHEEL_SPEED_SENSOR__PORT->IDR & WHEEL_SPEED_SENSOR__PIN;
		
		// check wheel speed sensor ticks counter min value
		if(ui16_wheel_speed_sensor_ticks) { ui16_wheel_speed_sensor_ticks_counter_min = ui16_wheel_speed_sensor_ticks >> 3; }
		else { ui16_wheel_speed_sensor_ticks_counter_min = WHEEL_SPEED_SENSOR_TICKS_COUNTER_MIN >> 3; }

		if(!ui8_wheel_speed_sensor_ticks_counter_started ||
		  (ui16_wheel_speed_sensor_ticks_counter > ui16_wheel_speed_sensor_ticks_counter_min)) {  
			// check if wheel speed sensor pin state has changed
			if (ui8_wheel_speed_sensor_pin_state != ui8_wheel_speed_sensor_pin_state_old) {
				// update old wheel speed sensor pin state
				ui8_wheel_speed_sensor_pin_state_old = ui8_wheel_speed_sensor_pin_state;

				// only consider the 0 -> 1 transition
				if (ui8_wheel_speed_sensor_pin_state) {
					// check if first transition
					if (!ui8_wheel_speed_sensor_ticks_counter_started) {
						// start wheel speed sensor ticks counter as this is the first transition
						ui8_wheel_speed_sensor_ticks_counter_started = 1;
					} else {
						// check if wheel speed sensor ticks counter is out of bounds
						if (ui16_wheel_speed_sensor_ticks_counter < WHEEL_SPEED_SENSOR_TICKS_COUNTER_MAX) {
							ui16_wheel_speed_sensor_ticks = 0;
							ui16_wheel_speed_sensor_ticks_counter = 0;
							ui8_wheel_speed_sensor_ticks_counter_started = 0;
						} else {
							ui16_wheel_speed_sensor_ticks = ui16_wheel_speed_sensor_ticks_counter;
							ui16_wheel_speed_sensor_ticks_counter = 0;
							++ui32_wheel_speed_sensor_ticks_total;
						}
					}
				}
			}
		}
		
        // increment and also limit the ticks counter
        if (ui8_wheel_speed_sensor_ticks_counter_started)
            if (ui16_wheel_speed_sensor_ticks_counter < WHEEL_SPEED_SENSOR_TICKS_COUNTER_MIN) {
                ++ui16_wheel_speed_sensor_ticks_counter;
            } else {
                // reset variables
                ui16_wheel_speed_sensor_ticks = 0;
                ui16_wheel_speed_sensor_ticks_counter = 0;
                ui8_wheel_speed_sensor_ticks_counter_started = 0;
            }
    }

    /****************************************************************************/

    // clears the TIM1 interrupt TIM1_IT_UPDATE pending bit
    TIM1->SR1 = (uint8_t) (~(uint8_t) TIM1_IT_CC4);
}


void hall_sensor_init(void) {
    GPIO_Init(HALL_SENSOR_A__PORT, (GPIO_Pin_TypeDef) HALL_SENSOR_A__PIN, GPIO_MODE_IN_FL_NO_IT);
    GPIO_Init(HALL_SENSOR_B__PORT, (GPIO_Pin_TypeDef) HALL_SENSOR_B__PIN, GPIO_MODE_IN_FL_NO_IT);
    GPIO_Init(HALL_SENSOR_C__PORT, (GPIO_Pin_TypeDef) HALL_SENSOR_C__PIN, GPIO_MODE_IN_FL_NO_IT);
}

void read_battery_voltage(void) {
#define READ_BATTERY_VOLTAGE_FILTER_COEFFICIENT   2

    /*---------------------------------------------------------
     NOTE: regarding filter coefficients

     Possible values: 0, 1, 2, 3, 4, 5, 6
     0 equals to no filtering and no delay, higher values
     will increase filtering but will also add a bigger delay.
     ---------------------------------------------------------*/

    static uint16_t ui16_adc_battery_voltage_accumulated;

    // low pass filter the voltage readed value, to avoid possible fast spikes/noise
    ui16_adc_battery_voltage_accumulated -= ui16_adc_battery_voltage_accumulated
            >> READ_BATTERY_VOLTAGE_FILTER_COEFFICIENT;
    ui16_adc_battery_voltage_accumulated += (uint16_t)(UI16_ADC_10_BIT_BATTERY_VOLTAGE);
    ui16_adc_battery_voltage_filtered = ui16_adc_battery_voltage_accumulated >> READ_BATTERY_VOLTAGE_FILTER_COEFFICIENT;
}

void calc_foc_angle(void) {
    static uint16_t ui16_foc_angle_accumulated;
    uint16_t ui16_temp;
    uint16_t ui16_e_phase_voltage;
    uint16_t ui16_i_phase_current_x2;
    uint16_t ui16_l_x1048576;
    uint32_t ui32_w_angular_velocity_x16;
    uint16_t ui16_iwl_128;

    // FOC implementation by calculating the angle between phase current and rotor magnetic flux (BEMF)
    // 1. phase voltage is calculate
    // 2. I*w*L is calculated, where I is the phase current. L was a measured value for 48V motor.
    // 3. inverse sin is calculated of (I*w*L) / phase voltage, were we obtain the angle
    // 4. previous calculated angle is applied to phase voltage vector angle and so the
    // angle between phase current and rotor magnetic flux (BEMF) is kept at 0 (max torque per amp)

    // calc E phase voltage
    ui16_temp = (uint8_t)BATTERY_VOLTAGE_PER_10_BIT_ADC_STEP_X512 * ui8_g_duty_cycle;
    ui16_e_phase_voltage = ((uint32_t)ui16_adc_battery_voltage_filtered * ui16_temp) >> 17;

    // calc I phase current
    if (ui8_g_duty_cycle > 10) {
        ui16_temp = ui8_adc_battery_current_filtered * (uint8_t)BATTERY_CURRENT_PER_10_BIT_ADC_STEP_X512;
        ui16_i_phase_current_x2 = ui16_temp / ui8_g_duty_cycle;
    } else {
        ui8_g_foc_angle = 0;
        goto skip_foc;
    }

    // calc W angular velocity: erps * 6.3
    // multiplication overflow if ui16_motor_speed_erps > 648 (65535/101) -> cast ui16_motor_speed_erps to uint32_t !!!!
    ui32_w_angular_velocity_x16 = (uint32_t)ui16_motor_speed_erps * 101U;

    // ---------------------------------------------------------------------------------------------------------------------
    // NOTE: EXPERIMENTAL and may not be good for the brushless motor inside TSDZ2
    // Original message from jbalat on 28.08.2018, about increasing the limits on 36 V motor -- please see that this seems to go over the recomended values
    // The ui32_l_x1048576 = 105 is working well so give that a try if you have a 36 V motor.
    // This is the minimum value that gives me 550 W of power when I have asked for 550 W at level 5 assist, > 36 km/hr
    //
    // Remember also to boost the max motor erps in main.h to get higher cadence
    // #define MOTOR_OVER_SPEED_ERPS 700 // motor max speed, protection max value | 30 points for the sinewave at max speed
    // ---------------------------------------------------------------------------------------------------------------------

    // ---------------------------------------------------------------------------------------------------------------------
    // 36 V motor: L = 76uH
    // 48 V motor: L = 135uH
    // ui32_l_x1048576 = 142; // 1048576 = 2^20 | 48V
    // ui32_l_x1048576 = 84; // 1048576 = 2^20 | 36V
    //
    // ui32_l_x1048576 = 142 <--- THIS VALUE WAS verified experimentaly on 2018.07 to be near the best value for a 48V motor
    // Test done with a fixed mechanical load, duty_cycle = 200 and 100 and measured battery current was 16 and 6 (10 and 4 amps)
    // ---------------------------------------------------------------------------------------------------------------------

    ui16_l_x1048576 = MOTOR_INDUCTANCE_x1048576;

    // calc IwL
    ui16_iwl_128 = ((uint32_t)((uint32_t)ui16_i_phase_current_x2 * ui16_l_x1048576) * ui32_w_angular_velocity_x16 ) >> 18;

    // calc FOC angle
    ui8_g_foc_angle = asin_table(ui16_iwl_128 / ui16_e_phase_voltage);
	
	uint8_t ui8_tmp = ui8_adc_battery_current_filtered / (uint8_t)13U;
	if (ui8_tmp > 5)
		ui8_tmp = 5;
	ui8_g_foc_angle += ui8_tmp;

    skip_foc:
    // low pass filter FOC angle
    ui16_foc_angle_accumulated -= ui16_foc_angle_accumulated >> 4;
    ui16_foc_angle_accumulated += ui8_g_foc_angle;
    ui8_g_foc_angle = ui16_foc_angle_accumulated >> 4;
}

// calc asin also converts the final result to degrees
uint8_t asin_table(uint8_t ui8_inverted_angle_x128) {
    uint8_t ui8_index = 0;

    while (ui8_index < SIN_TABLE_LEN) {
        if (ui8_inverted_angle_x128 < ui8_sin_table[ui8_index])
            break;

        ui8_index++;
    }

    // first value of table is 0 so ui8_index will always increment to at least 1 and return 0
    return ui8_index;
}

void motor_enable_pwm(void) {
    TIM1_OC1Init(TIM1_OCMODE_PWM1, TIM1_OUTPUTSTATE_ENABLE, TIM1_OUTPUTNSTATE_ENABLE, 255, // initial duty_cycle value
            TIM1_OCPOLARITY_HIGH, TIM1_OCPOLARITY_HIGH, TIM1_OCIDLESTATE_RESET, TIM1_OCIDLESTATE_SET);

    TIM1_OC2Init(TIM1_OCMODE_PWM1, TIM1_OUTPUTSTATE_ENABLE, TIM1_OUTPUTNSTATE_ENABLE, 255, // initial duty_cycle value
            TIM1_OCPOLARITY_HIGH, TIM1_OCPOLARITY_HIGH, TIM1_OCIDLESTATE_RESET, TIM1_OCIDLESTATE_SET);

    TIM1_OC3Init(TIM1_OCMODE_PWM1, TIM1_OUTPUTSTATE_ENABLE, TIM1_OUTPUTNSTATE_ENABLE, 255, // initial duty_cycle value
            TIM1_OCPOLARITY_HIGH, TIM1_OCPOLARITY_HIGH, TIM1_OCIDLESTATE_RESET, TIM1_OCIDLESTATE_SET);
}

void motor_disable_pwm(void) {
    TIM1_OC1Init(TIM1_OCMODE_PWM1, TIM1_OUTPUTSTATE_DISABLE, TIM1_OUTPUTNSTATE_DISABLE, 255, // initial duty_cycle value
            TIM1_OCPOLARITY_HIGH, TIM1_OCPOLARITY_HIGH, TIM1_OCIDLESTATE_RESET, TIM1_OCIDLESTATE_SET);

    TIM1_OC2Init(TIM1_OCMODE_PWM1, TIM1_OUTPUTSTATE_DISABLE, TIM1_OUTPUTNSTATE_DISABLE, 255, // initial duty_cycle value
            TIM1_OCPOLARITY_HIGH, TIM1_OCPOLARITY_HIGH, TIM1_OCIDLESTATE_RESET, TIM1_OCIDLESTATE_SET);

    TIM1_OC3Init(TIM1_OCMODE_PWM1, TIM1_OUTPUTSTATE_DISABLE, TIM1_OUTPUTNSTATE_DISABLE, 255, // initial duty_cycle value
            TIM1_OCPOLARITY_HIGH, TIM1_OCPOLARITY_HIGH, TIM1_OCIDLESTATE_RESET, TIM1_OCIDLESTATE_SET);
}
