/*
 * TongSheng TSDZ2 motor controller firmware/
 *
 * Copyright (C) Casainho, MSpider65 2020.
 *
 * Released under the GPL License, Version 3
 */

#ifndef INTERRUPTS_H_
#define INTERRUPTS_H_

#include "stm8s.h"

#define EXTI_HALL_A_IRQ  7              // ITC_IRQ_PORTE - Hall sensor A rise/fall detection
#define EXTI_HALL_B_IRQ  6              // ITC_IRQ_PORTD - Hall sensor B rise/fall detection
#define EXTI_HALL_C_IRQ  5              // ITC_IRQ_PORTC - Hall sensor C rise/fall detection
#define TIM1_CAP_COM_IRQHANDLER 12      // ITC_IRQ_TIM1_CAPCOM - PWM control loop (52us)
#define TIM4_OVF_IRQHANDLER 23          // ITC_IRQ_TIM4_OVF - TIM 4 overflow: 1ms counter
#define UART2_TX_IRQHANDLER 20          // UART2 TX
#define UART2_IRQHANDLER 21             // UART2 RX


// PWM cycle interrupt (called every 64us)
INTERRUPT_HANDLER(TIM1_CAP_COM_IRQHandler, TIM1_CAP_COM_IRQHANDLER);
// UART2 TX interrupt
INTERRUPT_HANDLER(UART2_TX_IRQHandler, UART2_TX_IRQHANDLER);
// UART2 RX interrupt
INTERRUPT_HANDLER(UART2_IRQHandler, UART2_IRQHANDLER);
// TIM4 Overflow interrupt (called every 1ms)
INTERRUPT_HANDLER(TIM4_IRQHandler, TIM4_OVF_IRQHANDLER);
// Hall Sensor Signal interrupt
INTERRUPT_HANDLER(HALL_SENSOR_A_PORT_IRQHandler, EXTI_HALL_A_IRQ);
INTERRUPT_HANDLER(HALL_SENSOR_B_PORT_IRQHandler, EXTI_HALL_B_IRQ);
INTERRUPT_HANDLER(HALL_SENSOR_C_PORT_IRQHandler, EXTI_HALL_C_IRQ);

#endif /* INTERRUPTS_H_ */
