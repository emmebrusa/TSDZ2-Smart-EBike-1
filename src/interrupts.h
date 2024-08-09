/*
 * TongSheng TSDZ2 motor controller firmware/
 *
 * Copyright (C) Casainho, MSpider65 2020.
 *
 * Released under the GPL License, Version 3
 */

#ifndef INTERRUPTS_H_
#define INTERRUPTS_H_


#define EXTI_HALL_A_IRQ  7              // ITC_IRQ_PORTE - Hall sensor A rise/fall detection
#define EXTI_HALL_B_IRQ  6              // ITC_IRQ_PORTD - Hall sensor B rise/fall detection
#define EXTI_HALL_C_IRQ  5              // ITC_IRQ_PORTC - Hall sensor C rise/fall detection
#define TIM1_CAP_COM_IRQHANDLER 12      // ITC_IRQ_TIM1_CAPCOM - PWM control loop (52us)
#define TIM4_OVF_IRQHANDLER 23          // ITC_IRQ_TIM4_OVF - TIM 4 overflow: 1ms counter
#define UART2_IRQHANDLER 21             // UART

#endif
