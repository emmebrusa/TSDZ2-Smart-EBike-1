/*
 * TongSheng TSDZ2 motor controller firmware/
 *
 * Copyright (C) Casainho, Leon, MSpider65 2020.
 *
 * Released under the GPL License, Version 3
 */

#include <stdint.h>
#include "main.h"
#include "motor.h"
#include "interrupts.h"
#include "stm8s_gpio.h"
#include "stm8s_tim1.h"
#include "ebike_app.h"
#include "pins.h"
#include "eeprom.h"

#define SVM_TABLE_LEN   256

#if PWM_FREQ == 19
// svm table 19 Khz
static const uint8_t ui8_svm_table[SVM_TABLE_LEN] = { 202, 203, 205, 206, 207, 208, 209, 210, 211, 211, 212, 213, 213,
        214, 214, 214, 215, 215, 215, 215, 215, 215, 215, 215, 214, 214, 214, 213, 213, 212, 211, 211, 210, 209, 208,
        208, 207, 206, 205, 204, 202, 201, 199, 195, 191, 187, 183, 178, 174, 170, 165, 161, 157, 152, 148, 143, 139,
        134, 130, 125, 121, 116, 112, 108, 103, 99, 94, 90, 85, 81, 76, 72, 67, 63, 58, 54, 50, 45, 41, 37, 32, 28, 24,
        20, 16, 14, 13, 11, 10, 9, 8, 7, 7, 6, 5, 4, 4, 3, 2, 2, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 2, 2, 3, 4,
        4, 5, 6, 7, 8, 9, 10, 12, 13, 14, 13, 12, 10, 9, 8, 7, 6, 5, 4, 4, 3, 2, 2, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1,
        1, 1, 2, 2, 3, 4, 4, 5, 6, 7, 7, 8, 9, 10, 11, 13, 14, 16, 20, 24, 28, 32, 37, 41, 45, 50, 54, 58, 63, 67, 72,
        76, 81, 85, 90, 94, 99, 103, 108, 112, 116, 121, 125, 130, 134, 139, 143, 148, 152, 157, 161, 165, 170, 174,
        178, 183, 187, 191, 195, 199, 201, 202, 204, 205, 206, 207, 208, 208, 209, 210, 211, 211, 212, 213, 213, 214,
        214, 214, 215, 215, 215, 215, 215, 215, 215, 215, 214, 214, 214, 213, 213, 212, 211, 211, 210, 209, 208, 207,
        206, 205, 203, 202, 201 };
#else
// svm table 18 Khz
static const uint8_t ui8_svm_table[SVM_TABLE_LEN] = { 208, 209, 210, 212, 213, 214, 215, 216, 217, 217, 218, 219, 219,
        220, 220, 220, 221, 221, 221, 221, 221, 221, 221, 221, 220, 220, 220, 219, 219, 218, 217, 217, 216, 215, 214,
        213, 212, 211, 210, 209, 208, 207, 205, 201, 196, 192, 188, 183, 179, 174, 170, 165, 161, 156, 152, 147, 143,
        138, 134, 129, 124, 120, 115, 111, 106, 101, 97, 92, 87, 83, 78, 74, 69, 65, 60, 56, 51, 47, 42, 38, 33, 29, 25,
        20, 16, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 4, 3, 2, 2, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 2, 2, 3, 4,
        4, 5, 6, 7, 8, 9, 11, 12, 13, 15, 13, 12, 11, 9, 8, 7, 6, 5, 4, 4, 3, 2, 2, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1,
        1, 1, 2, 2, 3, 4, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 16, 20, 25, 29, 33, 38, 42, 47, 51, 56, 60, 65, 69, 74,
        78, 83, 87, 92, 97, 101, 106, 111, 115, 120, 124, 129, 134, 138, 143, 147, 152, 156, 161, 165, 170, 174, 179,
        183, 188, 192, 196, 201, 205, 207, 208, 209, 210, 211, 212, 213, 214, 215, 216, 217, 217, 218, 219, 219, 220,
        220, 220, 221, 221, 221, 221, 221, 221, 221, 221, 220, 220, 220, 219, 219, 218, 217, 217, 216, 215, 214, 213,
        212, 210, 209, 208, 206 };
#endif

// motor variables
uint8_t ui8_hall_360_ref_valid = 0;
uint8_t ui8_motor_commutation_type = BLOCK_COMMUTATION;
static uint8_t ui8_motor_phase_absolute_angle;
volatile uint16_t ui16_hall_counter_total = 0xffff;

// power variables
volatile uint8_t ui8_controller_duty_cycle_ramp_up_inverse_step = PWM_DUTY_CYCLE_RAMP_UP_INVERSE_STEP_DEFAULT;
volatile uint8_t ui8_controller_duty_cycle_ramp_down_inverse_step = PWM_DUTY_CYCLE_RAMP_DOWN_INVERSE_STEP_DEFAULT;
volatile uint16_t ui16_adc_voltage_cut_off = 300*100/BATTERY_VOLTAGE_PER_10_BIT_ADC_STEP_X1000; // 30Volt default value
volatile uint8_t ui8_adc_battery_current_filtered = 0;
volatile uint8_t ui8_controller_adc_battery_current_target = 0;
volatile uint8_t ui8_g_duty_cycle = 0;
volatile uint8_t ui8_controller_duty_cycle_target = 0;
// Field Weakening Hall offset (added during interpolation)
volatile uint8_t ui8_fw_hall_counter_offset = 0;
volatile uint8_t ui8_fw_hall_counter_offset_max = 0;
volatile uint8_t ui8_field_weakening_enabled = 0;

// Duty cycle ramp up
static uint8_t ui8_counter_duty_cycle_ramp_up = 0;
static uint8_t ui8_counter_duty_cycle_ramp_down = 0;

// FOC angle
static uint8_t ui8_foc_angle_accumulated;
static uint8_t ui8_foc_flag;
static uint8_t ui8_g_foc_angle = 0;
static uint8_t ui8_foc_angle_multiplier = FOC_ANGLE_MULTIPLIER;
static uint8_t ui8_adc_foc_angle_current = 0;

// battery current variables
static uint8_t ui8_adc_battery_current_acc = 0;
static uint8_t ui8_adc_motor_phase_current = 0;

// ADC Values
volatile uint16_t ui16_adc_voltage;
volatile uint16_t ui16_adc_torque;
volatile uint16_t ui16_adc_throttle;

// brakes
volatile uint8_t ui8_brake_state = 0;

// cadence sensor
#define NO_PAS_REF 5
volatile uint16_t ui16_cadence_sensor_ticks = 0;
static uint16_t ui16_cadence_sensor_ticks_counter_min = CADENCE_SENSOR_CALC_COUNTER_MIN;
static uint8_t ui8_pas_state_old = 4;
static uint16_t ui16_cadence_calc_counter, ui16_cadence_stop_counter;
static uint8_t ui8_cadence_calc_ref_state = NO_PAS_REF;
const static uint8_t ui8_pas_old_valid_state[4] = { 0x01, 0x03, 0x00, 0x02 };

// wheel speed sensor
volatile uint16_t ui16_wheel_speed_sensor_ticks = 0;
volatile uint16_t ui16_wheel_speed_sensor_ticks_counter_min = 0;

// battery soc
volatile uint8_t ui8_battery_SOC_saved_flag = 0;
volatile uint8_t ui8_battery_SOC_reset_flag = 0;

// Measures did with a 24V Q85 328 RPM motor, rotating motor backwards by hand:
// Hall sensor A positivie to negative transition | BEMF phase B at max value / top of sinewave
// Hall sensor B positivie to negative transition | BEMF phase A at max value / top of sinewave
// Hall sensor C positive to negative transition | BEMF phase C at max value / top of sinewave

#ifdef TIME_DEBUG
volatile uint16_t ui16_pwm_cnt_down_irq;
volatile uint16_t ui16_pwm_cnt_up_irq = 0;
#endif

#ifdef HALL_DEBUG
volatile uint8_t ui8_hall_val_errors = 0;
volatile uint8_t ui8_hall_seq_errors = 0;
#endif

// PWM cycle interrupt
// TIM1 clock is 16MHz and count mode is "Center Aligned"
// Every cycle TIM1 counts up from 0 to 420 and then down from 420 to 0 (26.25+26.25us = 52.5us total time)
// The interrupt fires two times every cycle in the middle of the counter (when reaches 210 up and down)
// ADC conversion is automatically started by the rising edge of TRGO signal which is aligned with the Down interrupt signal.
// Both interrupts are used to read HAL sensors and update rotor position counters (max 26us rotor position offset error)
// and then:
// Up interrupt is used for:
//  - read and filter battery current
//  - read PAS sensor and cadence computation
//  - check brake (coaster brake and brake input signal)
//  - update duty cycle
// Down interrupt is used for:
//  - calculate rotor position (based on HAL sensors state and interpolation based on counters)
//  - Apply phase voltage and duty cycle to TIM1 outputs according to rotor position
//  - Read Wheel speed sensor and wheel speed computation

#ifdef __CDT_PARSER__
#define __interrupt(x)  // Disable Eclipse syntax check on interrupt keyword
#endif

volatile uint8_t ui8_hall_state_irq = 0;
volatile uint8_t ui8_hall_60_ref_irq[2];


// Interrupt routines called on Hall sensor state change (Highest priority)
// - read the Hall transition reference counter value (ui8_hall_60_ref_irq)
// - read the hall signal state (ui8_hall_state_irq)
//      - Hall A: bit 0
//      - Hall B: bit 1
//      - Hall C: bit 2
void HALL_SENSOR_A_PORT_IRQHandler(void)  __interrupt(EXTI_HALL_A_IRQ) {
    ui8_hall_60_ref_irq[0] = TIM3->CNTRH;
    ui8_hall_60_ref_irq[1] = TIM3->CNTRL;
    ui8_hall_state_irq &= (unsigned char)~0x01;
    if (HALL_SENSOR_A__PORT->IDR & HALL_SENSOR_A__PIN)
        ui8_hall_state_irq |= (unsigned char)0x01;
}

void HALL_SENSOR_B_PORT_IRQHandler(void) __interrupt(EXTI_HALL_B_IRQ)  {
    ui8_hall_60_ref_irq[0] = TIM3->CNTRH;
    ui8_hall_60_ref_irq[1] = TIM3->CNTRL;
    ui8_hall_state_irq &= (unsigned char)~0x02;
    if (HALL_SENSOR_B__PORT->IDR & HALL_SENSOR_B__PIN)
        ui8_hall_state_irq |= (unsigned char)0x02;
}

void HALL_SENSOR_C_PORT_IRQHandler(void) __interrupt(EXTI_HALL_C_IRQ)  {
    ui8_hall_60_ref_irq[0] = TIM3->CNTRH;
    ui8_hall_60_ref_irq[1] = TIM3->CNTRL;
    ui8_hall_state_irq &= (unsigned char)~0x04;
    if (HALL_SENSOR_C__PORT->IDR & HALL_SENSOR_C__PIN)
        ui8_hall_state_irq |= (unsigned char)0x04;
}

// Last rotor complete revolution Hall ticks
static uint16_t ui16_hall_360_ref;

// Last Hall sensor state
static uint8_t  ui8_hall_sensors_state_last = 7; // Invalid value, force execution of Hall code at the first run

// Hall counter value of last Hall transition
static uint16_t ui16_hall_60_ref_old;

// Hall Timer counter value calculated for the 6 different Hall transitions intervals
volatile uint16_t ui16_hall_calib_cnt[6];

// phase angle for rotor positions 30, 90, 150, 210, 270, 330 degrees
volatile uint8_t ui8_hall_ref_angles[6] = {
		PHASE_ROTOR_ANGLE_30,
		PHASE_ROTOR_ANGLE_90,
		PHASE_ROTOR_ANGLE_150,
		PHASE_ROTOR_ANGLE_210,
		PHASE_ROTOR_ANGLE_270,
		PHASE_ROTOR_ANGLE_330};

// Hall counter offset for states 6,2,3,1,5,4 (value configured from Android App)
volatile uint8_t ui8_hall_counter_offsets[6] = {
        HALL_COUNTER_OFFSET_UP,
        HALL_COUNTER_OFFSET_DOWN,
        HALL_COUNTER_OFFSET_UP,
        HALL_COUNTER_OFFSET_DOWN,
        HALL_COUNTER_OFFSET_UP,
        HALL_COUNTER_OFFSET_DOWN};

// Hall offset for current Hall state
static uint8_t ui8_hall_counter_offset;

// temporay variables (at the end of down irq stores phase a,b,c voltages)
static uint16_t ui16_a;
static uint16_t ui16_b;
static uint16_t ui16_c;

static uint8_t ui8_temp;

void TIM1_CAP_COM_IRQHandler(void) __interrupt(TIM1_CAP_COM_IRQHANDLER)
{
    // bit 5 of TIM1->CR1 contains counter direction (0=up, 1=down)
    if (TIM1->CR1 & 0x10) {
		#ifndef __CDT_PARSER__ // disable Eclipse syntax check
        __asm
            push cc             // save current Interrupt Mask (I1,I0 bits of CC register)
            sim                 // disable interrupts  (set I0,I1 bits of CC register to 1,1)
                                // Hall GPIO interrupt is buffered during this interval
            mov _ui8_temp+0, _ui8_hall_state_irq+0
            mov _ui16_b+0, _ui8_hall_60_ref_irq+0
            mov _ui16_b+1, _ui8_hall_60_ref_irq+1
            mov _ui16_a+0, 0x5328 // TIM3->CNTRH
            mov _ui16_a+1, 0x5329 // TIM3->CNTRL
            pop cc              // enable interrupts (restores previous value of Interrupt mask)
                                // Hall GPIO buffered interrupt could fire now
        __endasm;
        #endif
        // ui8_temp stores the current Hall sensor state
        // ui16_b stores the Hall sensor counter value of the last transition
        // ui16_a stores the current Hall sensor counter value

        /****************************************************************************/
        // run next code only when the hall state changes
        // hall sensors sequence with motor forward rotation: C, CB, B, BA, A, AC, ..
        // ui8_temp (hall sensor state):
        //      bit 0 0x01 Hall sensor A
        //      bit 1 0x02 Hall sensor B
        //      bit 2 0x04 Hall sensor C
        // ui8_hall_sensors_state sequence with motor forward rotation: 0x06, 0x02, 0x03, 0x01, 0x05, 0x04
        // rotor position:  30,   90,   150,  210,  270,  330 degrees
		
        if (ui8_hall_sensors_state_last != ui8_temp) {
            // Check first the state with the heaviest computation
            if (ui8_temp == 0x01) {
                // if (ui8_hall_360_ref_valid && (ui8_hall_sensors_state_last == 0x03)) {
                if (ui8_hall_sensors_state_last == ui8_hall_360_ref_valid) { // faster check
                    ui16_hall_counter_total = ui16_b - ui16_hall_360_ref;
                    ui8_motor_commutation_type = SINEWAVE_INTERPOLATION_60_DEGREES;
                }
                ui8_hall_360_ref_valid = 0x03;
                ui8_motor_phase_absolute_angle = ui8_hall_ref_angles[3]; // Rotor at 210 deg
                // set hall counter offset for rotor interpolation based on current hall state
                ui8_hall_counter_offset = ui8_hall_counter_offsets[3];
                ui16_hall_360_ref = ui16_b;
                // calculate hall ticks between the last two Hall transitions (for Hall calibration)
                ui16_hall_calib_cnt[3] = ui16_hall_360_ref - ui16_hall_60_ref_old;

                #ifdef HALL_DEBUG
                    if (ui8_hall_sensors_state_last != 0x03) {
                        ui8_hall_seq_errors++;
					}
                #endif
            }
			else {
                switch (ui8_temp) {
                    case 0x02:
                        ui8_motor_phase_absolute_angle = ui8_hall_ref_angles[1]; // Rotor at 90 deg
                        // set hall counter offset for rotor interpolation based on current hall state
                        ui8_hall_counter_offset = ui8_hall_counter_offsets[1];
                        // calculate hall ticks between the last two Hall transitions (for Hall calibration)
                        ui16_hall_calib_cnt[1] = ui16_b - ui16_hall_60_ref_old;

                        #ifdef HALL_DEBUG
							if (ui8_hall_sensors_state_last != 0x06) {
                                ui8_hall_seq_errors++;
							}
                        #endif
                        break;
                    case 0x03:
                        ui8_motor_phase_absolute_angle = ui8_hall_ref_angles[2]; // Rotor at 150 deg
                        ui8_hall_counter_offset = ui8_hall_counter_offsets[2];
                        ui16_hall_calib_cnt[2] = ui16_b - ui16_hall_60_ref_old;
                        // update ui8_g_foc_angle one time every ERPS
                        ui8_foc_flag = 1;

                        #ifdef HALL_DEBUG
                            if (ui8_hall_sensors_state_last != 0x02) {
                                ui8_hall_seq_errors++;
							}
                        #endif
                        break;
                    case 0x04:
                        ui8_motor_phase_absolute_angle = ui8_hall_ref_angles[5]; // Rotor at 330 deg
                        ui8_hall_counter_offset = ui8_hall_counter_offsets[5];
                        ui16_hall_calib_cnt[5] = ui16_b - ui16_hall_60_ref_old;

                        #ifdef HALL_DEBUG
                            if (ui8_hall_sensors_state_last != 0x05) {
                                ui8_hall_seq_errors++;
							}
                        #endif
                        break;
                    case 0x05:
                        ui8_motor_phase_absolute_angle = ui8_hall_ref_angles[4]; // Rotor at 270 deg
                        ui8_hall_counter_offset = ui8_hall_counter_offsets[4];
                        ui16_hall_calib_cnt[4] = ui16_b - ui16_hall_60_ref_old;

                        #ifdef HALL_DEBUG
                            if (ui8_hall_sensors_state_last != 0x01) {
                                ui8_hall_seq_errors++;
							}
                        #endif
                        break;
                    case 0x06:
                        ui8_motor_phase_absolute_angle = ui8_hall_ref_angles[0]; // Rotor at 30 deg
                        ui8_hall_counter_offset = ui8_hall_counter_offsets[0];
                        ui16_hall_calib_cnt[0] = ui16_b - ui16_hall_60_ref_old;

                        #ifdef HALL_DEBUG
                            if (ui8_hall_sensors_state_last != 0x04) {
                                ui8_hall_seq_errors++;
							}
                        #endif
                        break;
                    default:
                        #ifdef HALL_DEBUG
                            ui8_hall_val_errors++;
                        #endif
                        return;
                }
			}
            // update last hall sensor state
            #ifndef __CDT_PARSER__ // disable Eclipse syntax check
            __asm
                // speed optimization ldw, ldw -> mov,mov
                // ui16_hall_60_ref_old = ui16_b;
                mov _ui16_hall_60_ref_old+0, _ui16_b+0
                mov _ui16_hall_60_ref_old+1, _ui16_b+1
            __endasm;
            #endif
            ui8_hall_sensors_state_last = ui8_temp;
        }
		else {
            // Verify if rotor stopped (< 10 ERPS)
            // ui16_a - ui16_b = Hall counter ticks from the last Hall sensor transition;
            if ((ui16_a - ui16_b) > (HALL_COUNTER_FREQ/MOTOR_ROTOR_INTERPOLATION_MIN_ERPS/6)) {
                ui8_motor_commutation_type = BLOCK_COMMUTATION;
                ui8_g_foc_angle = 0;
                ui8_hall_360_ref_valid = 0;
                ui16_hall_counter_total = 0xffff;
            }
        }


        /****************************************************************************/
        // - calculate interpolation angle and sine wave table index

        /*
        ui8_temp = 0; // interpolation angle
        if (ui8_motor_commutation_type != BLOCK_COMMUTATION) {
            // ---------
            // uint8_t ui8_temp = ((uint32_t)ui16_a << 8) / ui16_hall_counter_total;
            // ---------
            // Avoid to use the slow _divulong library function.
            // Faster implementation of the above operation based on the following assumptions:
            // 1) ui16_a < 8192 (only 13 of 16 significants bits)
            // 2) LSB of (ui16_a << 8) is obviously 0x00
            // 3) The result should be less than 60 degrees. Use 180 deg (value of 128) to be safe.
            uint8_t ui8_cnt = 7; //max 6 loops: result < 128
            // Add Field Weakening counter offset (fw angle increases with rotor speed)
            // ui16_a - ui16_b = Hall counter ticks from the last Hall sensor transition;
            ui16_a = ((uint8_t)(ui8_fw_hall_counter_offset + ui8_hall_counter_offset) + (ui16_a - ui16_b)) << 1;

            do {
                ui16_a <<= 1;
                ui8_temp <<= 1;
                if (ui16_hall_counter_total <= ui16_a) {
                    ui16_a -= ui16_hall_counter_total;
                    ui8_temp |= (uint8_t)0x01;
                }
            } while (--ui8_cnt);
        }
        // we need to put phase voltage 90 degrees ahead of rotor position, to get current 90 degrees ahead and have max torque per amp
        ui8_svm_table_index = ui8_temp + ui8_motor_phase_absolute_angle + ui8_g_foc_angle;
        */
        #ifndef __CDT_PARSER__ // disable Eclipse syntax check
        __asm
            clr _ui8_temp+0
            tnz _ui8_motor_commutation_type+0
            jreq 00011$
            // ui16_a = ((ui16_a - ui16_b) + ui8_fw_hall_counter_offset + ui8_hall_counter_offset) << 2;
            ld  a, _ui8_fw_hall_counter_offset+0
            add a, _ui8_hall_counter_offset+0
            clrw    x
            ld  xl, a
            addw    x, _ui16_a+0
            subw    x, _ui16_b+0
            sllw x
            mov _ui16_b+0, #7
        00012$:
            sllw x
            sll  _ui8_temp+0
            cpw x, _ui16_hall_counter_total+0
            jrc  00013$
            bset    _ui8_temp+0, #0
            subw x, _ui16_hall_counter_total+0
        00013$:
            dec _ui16_b+0
            jrne 00012$
            // now ui8_temp contains the interpolation angle
        00011$: // BLOCK_COMMUTATION
            // ui8_temp = ui8_temp + ui8_motor_phase_absolute_angle + ui8_g_foc_angle;
            ld  a, _ui8_temp+0
            add a, _ui8_motor_phase_absolute_angle+0
            add a, _ui8_g_foc_angle+0
            ld _ui8_temp, a

        // now ui8_temp contains ui8_svm_table_index

        /****************************************************************************/
        // calculate final PWM duty_cycle values to be applied to TIMER1
        // scale and apply PWM duty_cycle for the 3 phases
        // phase A is advanced 240 degrees over phase B
        // Max of SVM table is 202 and ui8_tmp goes from 0 to 100 (101*254/256) and
        // ui8_phase_x_voltage goes from 0 (MIDDLE_PWM_COUNTER - ui8_temp) to 200 (MIDDLE_PWM_COUNTER + ui8_temp)

        /*
        // Phase A is advanced 240 degrees over phase B
        ui8_temp = ui8_svm_table[(uint8_t) (ui8_svm_table_index + 171)]; // 240 deg
        if (ui8_temp > MIDDLE_SVM_TABLE) {
            ui16_a = (uint16_t)((uint8_t)(ui8_temp - MIDDLE_SVM_TABLE) * (uint8_t)ui8_g_duty_cycle);
            ui16_a = (uint8_t)(MIDDLE_PWM_COUNTER + (uint8_t) (ui16_a >> 8)) << 1;
        } else {
            ui16_a = (uint16_t)((uint8_t)(MIDDLE_SVM_TABLE - ui8_temp) * (uint8_t)ui8_g_duty_cycle);
            ui16_a = (uint8_t)(MIDDLE_PWM_COUNTER - (uint8_t) (ui16_a >> 8)) << 1;
        }
        */

            // ui8_temp = ui8_svm_table[(uint8_t) (ui8_svm_table_index + 171)];
            add a, #0xab
            clrw x
            ld  xl, a
            ld  a, (_ui8_svm_table+0, x)
            cp  a, #MIDDLE_SVM_TABLE    // if (ui8_temp > MIDDLE_SVM_TABLE)
            jrule   00020$
            // ui16_a = (uint16_t)((uint8_t)(ui8_temp - MIDDLE_SVM_TABLE) * (uint8_t)ui8_g_duty_cycle);
            sub a, #MIDDLE_SVM_TABLE
            ld  xl, a
            ld  a, _ui8_g_duty_cycle+0
            mul x, a
            // ui16_a = (uint8_t)(MIDDLE_PWM_COUNTER + (uint8_t) (ui16_a >> 8)) << 1;
            ld  a, xh
            clr _ui16_a+0
            add a, #MIDDLE_PWM_COUNTER
            jrpl 00022$
            mov _ui16_a+0, #0x01  // result is negative (bit 7 is set)
        00022$:
            sll a
            ld  _ui16_a+1, a
            jra 00021$
        00020$:             // } else {
            // ui16_a = (uint16_t)((uint8_t)(MIDDLE_SVM_TABLE - ui8_temp) * (uint8_t)ui8_g_duty_cycle);
            sub a, #MIDDLE_SVM_TABLE
            neg a
            ld  xl, a
            ld  a, _ui8_g_duty_cycle+0
            mul x, a
            // ui16_a = (uint8_t)(MIDDLE_PWM_COUNTER - (uint8_t) (ui16_a >> 8)) << 1;
            ld  a, xh
            sub a, #MIDDLE_PWM_COUNTER
            clr _ui16_a+0
            neg a
            jrpl 00023$
            mov _ui16_a+0, #0x01
        00023$:
            sll a
            ld  _ui16_a+1, a
        00021$:

        /*
        // phase B as reference phase
        ui8_temp = ui8_svm_table[ui8_svm_table_index];
        if (ui8_temp > MIDDLE_SVM_TABLE) {
            ui16_b = (uint16_t) ((uint8_t)(ui8_temp - MIDDLE_SVM_TABLE) * (uint8_t)ui8_g_duty_cycle);
            ui16_b = (uint8_t)(MIDDLE_PWM_COUNTER + (uint8_t) (ui16_b >> 8)) << 1;
        } else {
            ui16_b = (uint16_t) ((uint8_t)(MIDDLE_SVM_TABLE - ui8_temp) * (uint8_t)ui8_g_duty_cycle);
            ui16_b = (uint8_t)(MIDDLE_PWM_COUNTER - (uint8_t)(ui16_b >> 8)) << 1;
        }
        */

            ld a, _ui8_temp+0   // ui8_svm_table_index is stored in ui8_temp
            clrw x              // ui8_temp = ui8_svm_table[ui8_svm_table_index];
            ld  xl, a
            ld  a, (_ui8_svm_table+0, x)
            cp  a, #MIDDLE_SVM_TABLE    // if (ui8_temp > MIDDLE_SVM_TABLE)
            jrule   00024$
            // ui16_b = (uint16_t)((uint8_t)(ui8_temp - MIDDLE_SVM_TABLE) * (uint8_t)ui8_g_duty_cycle);
            sub a, #MIDDLE_SVM_TABLE
            ld  xl, a
            ld  a, _ui8_g_duty_cycle+0
            mul x, a
            // ui16_b = (uint8_t)(MIDDLE_PWM_COUNTER + (uint8_t)(ui16_b >> 8)) << 1;
            ld  a, xh
            clr _ui16_b+0
            add a, #MIDDLE_PWM_COUNTER
            jrpl 00026$
            mov _ui16_b+0, #0x01
        00026$:
            sll a
            ld  _ui16_b+1, a
            jra 00025$
        00024$:             // } else {
            // ui16_b = (uint16_t)((uint8_t)(MIDDLE_SVM_TABLE - ui8_temp) * (uint8_t)ui8_g_duty_cycle);
            sub a, #MIDDLE_SVM_TABLE
            neg a
            ld  xl, a
            ld  a, _ui8_g_duty_cycle+0
            mul x, a
            // ui16_b = (uint8_t)(MIDDLE_PWM_COUNTER - (uint8_t) (ui16_b >> 8)) << 1;
            ld  a, xh
            sub a, #MIDDLE_PWM_COUNTER
            clr _ui16_b+0
            neg a
            jrpl 00027$
            mov _ui16_b+0, #0x01
        00027$:
            sll a
            ld  _ui16_b+1, a
        00025$:

        /*
        // phase C is advanced 120 degrees over phase B
        ui8_temp = ui8_svm_table[(uint8_t) (ui8_svm_table_index + 85 )]; // 120 deg
        if (ui8_temp > MIDDLE_SVM_TABLE) {
            ui16_c = (uint16_t) ((uint8_t)(ui8_temp - MIDDLE_SVM_TABLE) * (uint8_t)ui8_g_duty_cycle);
            ui16_c = (uint8_t)(MIDDLE_PWM_COUNTER + (uint8_t) (ui16_c >> 8)) << 1;
        } else {
            ui16_c = (uint16_t) ((uint8_t)(MIDDLE_SVM_TABLE - ui8_temp) * (uint8_t)ui8_g_duty_cycle);
            ui16_c = (uint8_t)(MIDDLE_PWM_COUNTER - (uint8_t) (ui16_c >> 8)) << 1;
        }
        */

            ld a, _ui8_temp+0     // ui8_svm_table_index is stored in ui8_temp
            add a, #0x55        // ui8_temp = ui8_svm_table[(uint8_t) (ui8_svm_table_index + 85 /* 120ยบ */)];
            clrw x
            ld  xl, a
            ld  a, (_ui8_svm_table+0, x)
            cp  a, #MIDDLE_SVM_TABLE    // if (ui8_temp > MIDDLE_SVM_TABLE)
            jrule   00028$
            // ui16_c = (uint16_t)((uint8_t)(ui8_temp - MIDDLE_SVM_TABLE) * (uint8_t)ui8_g_duty_cycle);
            sub a, #MIDDLE_SVM_TABLE
            ld  xl, a
            ld  a, _ui8_g_duty_cycle+0
            mul x, a
            // ui16_c = (uint8_t)(MIDDLE_PWM_COUNTER + (uint8_t)(ui16_c >> 8)) << 1;
            ld  a, xh
            clr _ui16_c+0
            add a, #MIDDLE_PWM_COUNTER
            jrpl 00030$
            mov _ui16_c+0, #0x01
        00030$:
            sll a
            ld  _ui16_c+1, a
            jra 00029$
        00028$:             // } else {
            // ui16_c = (uint16_t)((uint8_t)(MIDDLE_SVM_TABLE - ui8_temp) * (uint8_t)ui8_g_duty_cycle);
            sub a, #MIDDLE_SVM_TABLE
            neg a
            ld  xl, a
            ld  a, _ui8_g_duty_cycle+0
            mul x, a
            // ui16_c = (uint8_t)(MIDDLE_PWM_COUNTER - (uint8_t) (ui16_c >> 8)) << 1;
            ld  a, xh
            sub a, #MIDDLE_PWM_COUNTER
            clr _ui16_c+0
            neg a
            jrpl 00031$
            mov _ui16_c+0, #0x01
        00031$:
            sll a
            ld  _ui16_c+1, a
        00029$:
        __endasm;
        #endif

    #ifdef TIME_DEBUG
        #ifndef __CDT_PARSER__ // avoid Eclipse syntax check
        __asm
            ld  a, 0x5250
            and a, #0x10 // counter direction end irq
            or  a, 0x525e // TIM1->CNTRH
            ld  _ui16_pwm_cnt_down_irq+0, a      // ui16_pwm_cnt_down_irq MSB = TIM1->CNTRH | direction
            mov _ui16_pwm_cnt_down_irq+1, 0x525f // ui16_pwm_cnt_down_irq LSB = TIM1->CNTRL
        __endasm;
        #endif
    #endif

    }
	else {
        // CRITICAL SECTION !
        // Disable GPIO Hall interrupt during PWM counter update
        // The whole update is completed in 9 CPU cycles
        // set final duty_cycle value
        /*
        // phase B
        TIM1->CCR3H = (uint8_t)(ui16_b >> 8);
        TIM1->CCR3L = (uint8_t)(ui16_b);
        // phase C
        TIM1->CCR2H = (uint8_t)(ui16_c >> 8);
        TIM1->CCR2L = (uint8_t)(ui16_c);
        // phase A
        TIM1->CCR1H = (uint8_t)(ui16_a >> 8);
        TIM1->CCR1L = (uint8_t)(ui16_a);
        */
        #ifndef __CDT_PARSER__ // avoid Eclipse syntax check
        __asm
        push cc             // save current Interrupt Mask (I1,I0 bits of CC register)
        sim                 // disable interrupts  (set I0,I1 bits of CC register to 1,1)
                            // Hall GPIO interrupt is buffered during this interval
        mov 0x5269, _ui16_b+0
        mov 0x526a, _ui16_b+1
        mov 0x5267, _ui16_c+0
        mov 0x5268, _ui16_c+1
        mov 0x5265, _ui16_a+0
        mov 0x5266, _ui16_a+1
        pop cc           // enable interrupts (restores previous value of Interrupt mask)
                         // Hall GPIO buffered interrupt could fire now
        __endasm;
        #endif


        /****************************************************************************/
        /*
        // Read all ADC values (right aligned values).
        // No overrun errors can occurs here because the conversion is started at the beginning
        // of the PWM up interrupt and in this position is already ended.
        ui16_adc_voltage  = (*(uint16_t*)(0x53EC))
        ui16_adc_torque   = (*(uint16_t*)(0x53E8))
        ui16_adc_throttle = (*(uint16_t*)(0x53EE))
        ui8_temp = ADC1->DB5RL
        ui8_adc_battery_current_acc >>= 1;
        ui8_adc_battery_current_filtered >>= 1;
        ui8_adc_battery_current_acc = (uint8_t)(ui8_temp >> 1) + ui8_adc_battery_current_acc;
        ui8_adc_battery_current_filtered = (uint8_t)(ui8_adc_battery_current_acc >> 1) + ui8_adc_battery_current_filtered;
        // clear EOC flag (and select channel 7)
        ADC1->CSR = 0x07;

        // calculate motor phase current ADC value and update ui8_g_foc_angle
        if (ui8_g_duty_cycle > 0) {
            ui8_adc_motor_phase_current = (uint16_t)((uint16_t)((uint16_t)ui8_adc_battery_current_filtered << 8)) / ui8_g_duty_cycle;
            if (ui8_foc_flag) {
				ui8_adc_foc_angle_current = (ui8_adc_battery_current_filtered >> 1) + (ui8_adc_motor_phase_current >> 1);
                ui8_foc_flag = (uint16_t)(ui8_adc_foc_angle_current * ui8_foc_angle_multiplier) / 256;
                if (ui8_foc_flag > 13)
                    ui8_foc_flag = 13;
                ui8_foc_angle_accumulated = ui8_foc_angle_accumulated - (ui8_foc_angle_accumulated >> 4) + ui8_foc_flag;
                ui8_g_foc_angle = ui8_foc_angle_accumulated >> 4;
                ui8_foc_flag = 0;
            }
        } else {
            ui8_adc_motor_phase_current = 0;
            if (ui8_foc_flag) {
                ui8_foc_angle_accumulated = ui8_foc_angle_accumulated - (ui8_foc_angle_accumulated >> 4);
                ui8_g_foc_angle = ui8_foc_angle_accumulated >> 4;
                ui8_foc_flag = 0;
            }
        }
        */

        #ifndef __CDT_PARSER__ // avoid Eclipse syntax check
        __asm
        ldw x, 0x53EC
        ldw _ui16_adc_voltage, x
        ldw x, 0x53E8
        ldw _ui16_adc_torque, x
        ldw x, 0x53EE
        ldw _ui16_adc_throttle, x
        ld  a, 0x53EB                               // ui8_temp |= ADC1->DB5RL;
        srl _ui8_adc_battery_current_acc+0          // ui8_adc_battery_current_acc >>= 1;
        srl a                                       // ui8_adc_battery_current_acc = (uint8_t)(ui8_temp >> 1) + ui8_adc_battery_current_acc;
        add a, _ui8_adc_battery_current_acc+0
        ld  _ui8_adc_battery_current_acc+0, a
        srl _ui8_adc_battery_current_filtered+0     // ui8_adc_battery_current_filtered >>= 1;
        srl a                                       // ui8_adc_battery_current_filtered = (uint8_t)(ui8_adc_battery_current_acc >> 1) + ui8_adc_battery_current_filtered;
        add a, _ui8_adc_battery_current_filtered+0
        ld  _ui8_adc_battery_current_filtered+0, a
        mov 0x5400+0, #0x07                         // ADC1->CSR = 0x07;

        tnz _ui8_g_duty_cycle+0                     // if (ui8_g_duty_cycle > 0)
        jreq 00051$
        clrw x          // ui8_adc_motor_phase_current = (ui8_adc_battery_current_filtered << 8)) / ui8_g_duty_cycle;
        ld  xh, a
        ld  a, _ui8_g_duty_cycle+0
        div x, a
        ld  a, xl
        ld  _ui8_adc_motor_phase_current+0, a
		
		mov	_ui8_adc_foc_angle_current+0, _ui8_adc_battery_current_filtered+0
		srl	_ui8_adc_foc_angle_current+0
		ld	a, _ui8_adc_motor_phase_current+0
		srl	a
		add	a, _ui8_adc_foc_angle_current+0
		ld	_ui8_adc_foc_angle_current+0, a
		
        tnz _ui8_foc_flag+0 // if (ui8_foc_flag)
        jreq 00052$
        // ui8_foc_flag = (uint16_t)(ui8_adc_foc_angle_current * ui8_foc_angle_multiplier) / 256;
		ld  a, _ui8_adc_foc_angle_current+0
		clrw x
		ld  xl, a
		ld  a, _ui8_foc_angle_multiplier+0
        mul x, a
        ld  a, xh
        ld  _ui8_foc_flag+0, a
        cp  a, #0x0d    // if (ui8_foc_flag > 13)
        jrule   00053$
        mov _ui8_foc_flag+0, #0x0d
    00053$:
        // ui8_foc_angle_accumulated = ui8_foc_angle_accumulated - (ui8_foc_angle_accumulated >> 4) + ui8_foc_flag;
        ld  a, _ui8_foc_angle_accumulated+0
        swap a
        and a, #0x0f
        neg a
        add a, _ui8_foc_angle_accumulated+0
        add a, _ui8_foc_flag+0
        ld  _ui8_foc_angle_accumulated+0, a
        // ui8_g_foc_angle = ui8_foc_angle_accumulated >> 4;
        swap a
        and a, #0x0f
        ld _ui8_g_foc_angle+0, a
        clr _ui8_foc_flag+0
        jra 00052$
    00051$:
        clr _ui8_adc_motor_phase_current+0      // ui8_adc_motor_phase_current = 0;
        clr _ui8_adc_foc_angle_current+0			// ui8_adc_foc_angle_current = 0;
		tnz _ui8_foc_flag+0   // if (ui8_foc_flag)
        jreq 00052$
        // ui8_foc_angle_accumulated = ui8_foc_angle_accumulated - (ui8_foc_angle_accumulated >> 4);
        ld  a, _ui8_foc_angle_accumulated+0
        swap a
        and a, #0x0f
        neg a
        add a, _ui8_foc_angle_accumulated+0
        ld  _ui8_foc_angle_accumulated+0, a
        // ui8_g_foc_angle = ui8_foc_angle_accumulated >> 4;
        swap a
        and a, #0x0f
        ld _ui8_g_foc_angle+0, a
        clr _ui8_foc_flag+0
    00052$:
        __endasm;
        #endif
		
        /****************************************************************************/
        // brake state (used also in ebike_app loop)
        // - check if coaster brake is engaged
        // - check if brakes are engaged

		#if COASTER_BRAKE_ENABLED
        // check if coaster brake is engaged
        if (ui16_adc_torque < ui16_adc_coaster_brake_threshold) {
            // set brake state
            ui8_brake_state = 1;
        }
		else {
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
        // PWM duty_cycle controller:
        // - limit battery undervolt
        // - limit battery max current
        // - limit motor max phase current
        // - limit motor max ERPS
        // - ramp up/down PWM duty_cycle and/or field weakening angle value

        // check if to decrease, increase or maintain duty cycle
        if ( (ui8_controller_duty_cycle_target < ui8_g_duty_cycle)
          || (ui8_controller_adc_battery_current_target < ui8_adc_battery_current_filtered)
          || (ui8_adc_motor_phase_current > ui8_adc_motor_phase_current_max)
          || (ui16_hall_counter_total < (HALL_COUNTER_FREQ / MOTOR_OVER_SPEED_ERPS))
          || (ui16_adc_voltage < ui16_adc_voltage_cut_off)
          || (ui8_brake_state)) {
			
            // reset duty cycle ramp up counter (filter)
            ui8_counter_duty_cycle_ramp_up = 0;
			
            // ramp down duty cycle
            if (++ui8_counter_duty_cycle_ramp_down > ui8_controller_duty_cycle_ramp_down_inverse_step) {
                ui8_counter_duty_cycle_ramp_down = 0;
                // decrement field weakening angle if set or duty cycle if not
                if (ui8_fw_hall_counter_offset > 0) {
                    ui8_fw_hall_counter_offset--;
                }
				else if (ui8_g_duty_cycle > 0) {
                    ui8_g_duty_cycle--;
				}
            }
        }
		else if ((ui8_controller_duty_cycle_target > ui8_g_duty_cycle)
          && (ui8_controller_adc_battery_current_target > ui8_adc_battery_current_filtered)) {
            // reset duty cycle ramp down counter (filter)
            ui8_counter_duty_cycle_ramp_down = 0;

            // ramp up duty cycle
            if (++ui8_counter_duty_cycle_ramp_up > ui8_controller_duty_cycle_ramp_up_inverse_step) {
                ui8_counter_duty_cycle_ramp_up = 0;

                // increment duty cycle
                if (ui8_g_duty_cycle < PWM_DUTY_CYCLE_MAX) {
                    ui8_g_duty_cycle++;
                }
            }
        }
		else if ((ui8_field_weakening_enabled)
				&& (ui8_g_duty_cycle == PWM_DUTY_CYCLE_MAX)) {
            // reset duty cycle ramp down counter (filter)
            ui8_counter_duty_cycle_ramp_down = 0;

            if (++ui8_counter_duty_cycle_ramp_up > ui8_controller_duty_cycle_ramp_up_inverse_step) {
               ui8_counter_duty_cycle_ramp_up = 0;

               // increment field weakening angle
               if (ui8_fw_hall_counter_offset < ui8_fw_hall_counter_offset_max) {
                   ui8_fw_hall_counter_offset++;
			   }
            }
        }
		else {
            // duty cycle is where it needs to be so reset ramp counters (filter)
            ui8_counter_duty_cycle_ramp_up = 0;
            ui8_counter_duty_cycle_ramp_down = 0;
        }


        /****************************************************************************/
        // Wheel speed sensor detection

        static uint16_t ui16_wheel_speed_sensor_ticks_counter;
        static uint8_t ui8_wheel_speed_sensor_ticks_counter_started;
        static uint8_t ui8_wheel_speed_sensor_pin_state_old;

        // check wheel speed sensor pin state
        ui8_temp = WHEEL_SPEED_SENSOR__PORT->IDR & WHEEL_SPEED_SENSOR__PIN;

		// check wheel speed sensor ticks counter min value
		if (ui16_wheel_speed_sensor_ticks) { ui16_wheel_speed_sensor_ticks_counter_min = ui16_wheel_speed_sensor_ticks >> 3; }
		else { ui16_wheel_speed_sensor_ticks_counter_min = WHEEL_SPEED_SENSOR_TICKS_COUNTER_MIN >> 3; }

		if (!ui8_wheel_speed_sensor_ticks_counter_started ||
		  (ui16_wheel_speed_sensor_ticks_counter > ui16_wheel_speed_sensor_ticks_counter_min)) { 
			// check if wheel speed sensor pin state has changed
			if (ui8_temp != ui8_wheel_speed_sensor_pin_state_old) {
				// update old wheel speed sensor pin state
				ui8_wheel_speed_sensor_pin_state_old = ui8_temp;

				// only consider the 0 -> 1 transition
				if (ui8_temp) {
					// check if first transition
					if (!ui8_wheel_speed_sensor_ticks_counter_started) {
						// start wheel speed sensor ticks counter as this is the first transition
						ui8_wheel_speed_sensor_ticks_counter_started = 1;
					}
					else {
						// check if wheel speed sensor ticks counter is out of bounds
						if (ui16_wheel_speed_sensor_ticks_counter < WHEEL_SPEED_SENSOR_TICKS_COUNTER_MAX) {
							ui16_wheel_speed_sensor_ticks = 0;
							ui16_wheel_speed_sensor_ticks_counter = 0;
							ui8_wheel_speed_sensor_ticks_counter_started = 0;
						}
						else {
							ui16_wheel_speed_sensor_ticks = ui16_wheel_speed_sensor_ticks_counter;
							ui16_wheel_speed_sensor_ticks_counter = 0;
						}
					}
				}
			}
		}

        // increment and also limit the ticks counter
        if (ui8_wheel_speed_sensor_ticks_counter_started)
            if (ui16_wheel_speed_sensor_ticks_counter < WHEEL_SPEED_SENSOR_TICKS_COUNTER_MIN) {
                ++ui16_wheel_speed_sensor_ticks_counter;
            }
			else {
                // reset variables
                ui16_wheel_speed_sensor_ticks = 0;
                ui16_wheel_speed_sensor_ticks_counter = 0;
                ui8_wheel_speed_sensor_ticks_counter_started = 0;
            }


        /****************************************************************************/
        /*
         * - New pedal start/stop detection Algorithm (by MSpider65) -
         *
         * Pedal start/stop detection uses both transitions of both PAS sensors
         * ui8_temp stores the PAS1 and PAS2 state: bit0=PAS1,  bit1=PAS2
         * Pedal forward ui8_temp sequence is: 0x01 -> 0x00 -> 0x02 -> 0x03 -> 0x01
         * After a stop, the first forward transition is taken as reference transition
         * Following forward transition sets the cadence to 7RPM for immediate startup
         * Then, starting form the second reference transition, the cadence is calculated based on counter value
         * All transitions are a reference for the stop detection counter (4 time faster stop detection):
         */
        ui8_temp = 0;
        if (PAS1__PORT->IDR & PAS1__PIN) {
            ui8_temp |= (unsigned char)0x01;
		}
        if (PAS2__PORT->IDR & PAS2__PIN) {
            ui8_temp |= (unsigned char)0x02;
		}
        if (ui8_temp != ui8_pas_state_old) {
            if (ui8_pas_state_old != ui8_pas_old_valid_state[ui8_temp]) {
                // wrong state sequence: backward rotation
                ui16_cadence_sensor_ticks = 0;
                ui8_cadence_calc_ref_state = NO_PAS_REF;
                goto skip_cadence;
            }
			
			ui16_cadence_sensor_ticks_counter_min = ui16_cadence_ticks_count_min_speed_adj;
			
            if (ui8_temp == ui8_cadence_calc_ref_state) {
                // ui16_cadence_calc_counter is valid for cadence calculation
                ui16_cadence_sensor_ticks = ui16_cadence_calc_counter;
                ui16_cadence_calc_counter = 0;
                // software based Schmitt trigger to stop motor jitter when at resolution limits
                ui16_cadence_sensor_ticks_counter_min += CADENCE_SENSOR_STANDARD_MODE_SCHMITT_TRIGGER_THRESHOLD;
            }
			else if (ui8_cadence_calc_ref_state == NO_PAS_REF) {
                // this is the new reference state for cadence calculation
                ui8_cadence_calc_ref_state = ui8_temp;
                ui16_cadence_calc_counter = 0;
            }
			else if (ui16_cadence_sensor_ticks == 0) {
                // Waiting the second reference transition: set the cadence to 7 RPM for immediate start
                ui16_cadence_sensor_ticks = CADENCE_TICKS_STARTUP;
            }

            skip_cadence:
            // reset the counter used to detect pedal stop
            ui16_cadence_stop_counter = 0;
            // save current PAS state
            ui8_pas_state_old = ui8_temp;
        }

        if (++ui16_cadence_stop_counter > ui16_cadence_sensor_ticks_counter_min) {
            // pedals stop detected
            ui16_cadence_sensor_ticks = 0;
            ui16_cadence_stop_counter = 0;
            ui8_cadence_calc_ref_state = NO_PAS_REF;
        }
		else if (ui8_cadence_calc_ref_state != NO_PAS_REF) {
            // increment cadence tick counter
            ++ui16_cadence_calc_counter;
        }

        #ifdef TIME_DEBUG
            #ifndef __CDT_PARSER__ // avoid Eclipse syntax check
            __asm
                ld  a, 0x5250
                and a, #0x10 // counter direction end irq
                or  a, 0x525e // TIM1->CNTRH
                ld  _ui16_pwm_cnt_up_irq+0, a      // ui16_pwm_cnt_up_irq MSB = TIM1->CNTRH | direction
                mov _ui16_pwm_cnt_up_irq+1, 0x525f // ui16_pwm_cnt_up_irq LSB = TIM1->CNTRL
            __endasm;
            #endif
        #endif
    }
	
	// save percentage remaining battery capacity at shutdown
	struct_configuration_variables *p_configuration_variables;
    p_configuration_variables = get_configuration_variables();
	
	if ((ui16_adc_voltage < BATTERY_VOLTAGE_SHUTDOWN_10_BIT)
		&&(!ui8_battery_SOC_saved_flag)
		&&(ui8_battery_SOC_reset_flag))
	{
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
			
		// battery SOC saved
		ui8_battery_SOC_saved_flag = 1;
	}
	
    /****************************************************************************/
    irq_end:
    // clears the TIM1 interrupt TIM1_IT_UPDATE pending bit
    TIM1->SR1 = (uint8_t) (~(uint8_t) TIM1_IT_CC4);
}


void hall_sensor_init(void) {
    // Init Hall sensor GPIO
    GPIO_Init(HALL_SENSOR_A__PORT, (GPIO_Pin_TypeDef) HALL_SENSOR_A__PIN, GPIO_MODE_IN_FL_IT);
    GPIO_Init(HALL_SENSOR_B__PORT, (GPIO_Pin_TypeDef) HALL_SENSOR_B__PIN, GPIO_MODE_IN_FL_IT);
    GPIO_Init(HALL_SENSOR_C__PORT, (GPIO_Pin_TypeDef) HALL_SENSOR_C__PIN, GPIO_MODE_IN_FL_IT);

    ui8_hall_state_irq = 0;
    if (HALL_SENSOR_A__PORT->IDR & HALL_SENSOR_A__PIN) {
        ui8_hall_state_irq |= (unsigned char)0x01;
	}
    if (HALL_SENSOR_B__PORT->IDR & HALL_SENSOR_B__PIN) {
        ui8_hall_state_irq |= (unsigned char)0x02;
	}
    if (HALL_SENSOR_C__PORT->IDR & HALL_SENSOR_C__PIN) {
        ui8_hall_state_irq |= (unsigned char)0x04;
	}
	
    // Hall GPIO priority = 3. Priority increases from 1 (min priority) to 3 (max priority)
    ITC_SetSoftwarePriority(EXTI_HALL_A_IRQ, ITC_PRIORITYLEVEL_3);
    ITC_SetSoftwarePriority(EXTI_HALL_B_IRQ, ITC_PRIORITYLEVEL_3);
    ITC_SetSoftwarePriority(EXTI_HALL_C_IRQ, ITC_PRIORITYLEVEL_3);

    // Hall GPIO signal interrupt sensitivity on both rising and falling edges
    EXTI_SetExtIntSensitivity(EXTI_PORT_GPIOC, EXTI_SENSITIVITY_RISE_FALL);
    EXTI_SetExtIntSensitivity(EXTI_PORT_GPIOD, EXTI_SENSITIVITY_RISE_FALL);
    EXTI_SetExtIntSensitivity(EXTI_PORT_GPIOE, EXTI_SENSITIVITY_RISE_FALL);
    EXTI_SetTLISensitivity(EXTI_TLISENSITIVITY_FALL_ONLY);
}


void motor_enable_pwm(void) {
    TIM1_OC1Init(TIM1_OCMODE_PWM1, TIM1_OUTPUTSTATE_ENABLE, TIM1_OUTPUTNSTATE_ENABLE, 128, // initial duty_cycle value
            TIM1_OCPOLARITY_HIGH, TIM1_OCPOLARITY_HIGH, TIM1_OCIDLESTATE_RESET, TIM1_OCIDLESTATE_SET);

    TIM1_OC2Init(TIM1_OCMODE_PWM1, TIM1_OUTPUTSTATE_ENABLE, TIM1_OUTPUTNSTATE_ENABLE, 128, // initial duty_cycle value
            TIM1_OCPOLARITY_HIGH, TIM1_OCPOLARITY_HIGH, TIM1_OCIDLESTATE_RESET, TIM1_OCIDLESTATE_SET);

    TIM1_OC3Init(TIM1_OCMODE_PWM1, TIM1_OUTPUTSTATE_ENABLE, TIM1_OUTPUTNSTATE_ENABLE, 128, // initial duty_cycle value
            TIM1_OCPOLARITY_HIGH, TIM1_OCPOLARITY_HIGH, TIM1_OCIDLESTATE_RESET, TIM1_OCIDLESTATE_SET);
}

void motor_disable_pwm(void) {
    TIM1_OC1Init(TIM1_OCMODE_PWM1, TIM1_OUTPUTSTATE_DISABLE, TIM1_OUTPUTNSTATE_DISABLE, 128, // initial duty_cycle value
            TIM1_OCPOLARITY_HIGH, TIM1_OCPOLARITY_HIGH, TIM1_OCIDLESTATE_RESET, TIM1_OCIDLESTATE_SET);

    TIM1_OC2Init(TIM1_OCMODE_PWM1, TIM1_OUTPUTSTATE_DISABLE, TIM1_OUTPUTNSTATE_DISABLE, 128, // initial duty_cycle value
            TIM1_OCPOLARITY_HIGH, TIM1_OCPOLARITY_HIGH, TIM1_OCIDLESTATE_RESET, TIM1_OCIDLESTATE_SET);

    TIM1_OC3Init(TIM1_OCMODE_PWM1, TIM1_OUTPUTSTATE_DISABLE, TIM1_OUTPUTNSTATE_DISABLE, 128, // initial duty_cycle value
            TIM1_OCPOLARITY_HIGH, TIM1_OCPOLARITY_HIGH, TIM1_OCIDLESTATE_RESET, TIM1_OCIDLESTATE_SET);
}
