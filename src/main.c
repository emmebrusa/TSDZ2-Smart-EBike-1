/*
 * TongSheng TSDZ2 motor controller firmware/
 *
 * Copyright (C) Casainho, Leon, MSpider65 2020.
 *
 * Released under the GPL License, Version 3
 */

#include <stdint.h>
#include "interrupts.h"
#include "stm8s.h"
#include "uart.h"
#include "pwm.h"
#include "motor.h"
#include "wheel_speed_sensor.h"
#include "brake.h"
#include "pas.h"
#include "adc.h"
#include "timers.h"
#include "ebike_app.h"
#include "torque_sensor.h"
#include "eeprom.h"
#include "lights.h"

/////////////////////////////////////////////////////////////////////////////////////////////
//// Functions prototypes

// main -- start of firmware and main loop
int main(void);

// With SDCC, interrupt service routine function prototypes must be placed in the file that contains main ()
// in order for an vector for the interrupt to be placed in the the interrupt vector space.  It's acceptable
// to place the function prototype in a header file as long as the header file is included in the file that
// contains main ().  SDCC will not generate any warnings or errors if this is not done, but the vector will
// not be in place so the ISR will not be executed when the interrupt occurs.

// Calling a function from interrupt not always works, SDCC manual says to avoid it. Maybe the best is to put
// all the code inside the interrupt

// Local VS global variables
// Sometimes I got the following error when compiling the firmware: motor.asm:750: Error: <r> relocation error
// when I have this code inside a function: "static uint8_t ui8_example_counter = 0;"
// and the solution was define the variable as global instead
// Another error example:
// *** buffer overflow detected ***: sdcc terminated
// Caught signal 6: SIGABRT

#ifdef __CDT_PARSER__
#define __interrupt(x)
#endif
// PWM cycle interrupt (called every 64us)
void TIM1_CAP_COM_IRQHandler(void) __interrupt(TIM1_CAP_COM_IRQHANDLER);
// UART interrupt
void UART2_IRQHandler(void) __interrupt(UART2_IRQHANDLER);
// TIM4 Overflow interrupt (called every 1ms)
void TIM4_IRQHandler(void) __interrupt(TIM4_OVF_IRQHANDLER);
// Hall Sensor Signal interrupt
void HALL_SENSOR_A_PORT_IRQHandler(void) __interrupt(EXTI_HALL_A_IRQ);
void HALL_SENSOR_B_PORT_IRQHandler(void) __interrupt(EXTI_HALL_B_IRQ);
void HALL_SENSOR_C_PORT_IRQHandler(void) __interrupt(EXTI_HALL_C_IRQ);

/////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////

#ifdef TIME_DEBUG
static uint8_t ui8_main_time;
uint8_t ui8_max_ebike_time = 0;
#endif


static uint8_t ui8_1ms_counter = 0;
static uint8_t ui8_ebike_app_controller_counter = 0;

int main(void) {
    // set clock at the max 16 MHz
    CLK_HSIPrescalerConfig(CLK_PRESCALER_HSIDIV1);

    brake_init();
    /* ++++++++++++++
    while (GPIO_ReadInputPin(BRAKE__PORT, BRAKE__PIN) == 0)
        ; // hold here while brake is pressed -- this is a protection for development
    */
    adc_init();
    lights_init();
    uart2_init();
    timers_init();
    torque_sensor_init();
    pas_init();
    wheel_speed_sensor_init();
    pwm_init();
    hall_sensor_init();
	EEPROM_init();
    enableInterrupts();
	ebike_app_init();

    while (1) {
        ui8_1ms_counter = ui8_tim4_counter;

        // run every 25ms. Max measured ebike_app_controller() duration is 3,1 ms.
        if ((uint8_t)(ui8_1ms_counter - ui8_ebike_app_controller_counter) >= 25U) {

            #ifdef TIME_DEBUG
            // incremented every 50us by PWM interrupt function
            ui8_main_time = 0;
            #endif

            ui8_ebike_app_controller_counter = ui8_1ms_counter;
            ebike_app_controller();

            #ifdef TIME_DEBUG
            ui8_main_time = ui8_tim4_counter - ui8_main_time
            if (ui8_main_time > ui8_max_ebike_time) {
                ui8_max_ebike_time = ui8_main_time;
			}
            #endif
        }
    }
}
