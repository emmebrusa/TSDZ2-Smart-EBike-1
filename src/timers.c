/*
 * TongSheng TSDZ2 motor controller firmware/
 *
 * Copyright (C) Casainho, 2018.
 *
 * Released under the GPL License, Version 3
 */

#include "timers.h"
#include "stm8s.h"
#include "interrupts.h"

volatile uint8_t ui8_tim4_counter = 0;

#ifdef __CDT_PARSER__
#define __interrupt(x)
#endif

void timer2_init(void);
void timer3_init(void);
void timer4_init(void);

void timers_init(void) {
    timer2_init();
    timer3_init();
    timer4_init();
}

// Timer2 is used to create the pulse signal for excitation of the torque sensor circuit
// Pulse signal: period of 20us, Ton = 2us, Toff = 18us
void timer2_init(void) {
    uint16_t ui16_i;

    // Timer2 clock = 16MHz; target: 20us period --> 50khz
    // counter period = (1 / (16000000 / prescaler)) * (159 + 1) = 20us
    TIM2_TimeBaseInit(TIM2_PRESCALER_2, 159);

    // pulse of 2us
    TIM2_OC2Init(TIM2_OCMODE_PWM1,
            TIM2_OUTPUTSTATE_ENABLE,
            16,
            TIM2_OCPOLARITY_HIGH);
    TIM2_OC2PreloadConfig(ENABLE);

    TIM2_ARRPreloadConfig(ENABLE);

    TIM2_Cmd(ENABLE);

    // IMPORTANT: this software delay is needed so timer2 work after this
    for (ui16_i = 0; ui16_i < (65000); ui16_i++) {
        ;
    }
}

// HALL sensor time counter (250 KHz, 4us period, 1deg resolution at max rotor speed of 660ERPS)
// Counter is used to measure the time between Hall sensors transitions.
// Hall sensor GPIO IRQ is used to read counter reference value at every Hall sensor transition
void timer3_init(void) {
    uint16_t ui16_i;

    // TIM3 Peripheral Configuration
    TIM3_DeInit();
    TIM3_TimeBaseInit(TIM3_PRESCALER_64, 0xffff); // 16MHz/64=250KHz
    TIM3_Cmd(ENABLE); // TIM3 counter enable

    // IMPORTANT: this software delay is needed so timer3 work after this
    for (ui16_i = 0; ui16_i < (65000); ui16_i++) {
        ;
    }
}

// TIM4 configuration used to generate a 1ms counter (Counter overflow every 1ms)
void timer4_init(void) {
    uint16_t ui16_i;

    TIM4_DeInit();
    TIM4_TimeBaseInit(TIM4_PRESCALER_128, 0x7d); // Freq = 16MHz/128*125=1KHz (1ms)
    ITC_SetSoftwarePriority(TIM4_OVF_IRQHANDLER, ITC_PRIORITYLEVEL_1); // 1 = lowest priority
    TIM4_ITConfig(TIM4_IT_UPDATE, ENABLE); // Enable Update/Overflow Interrupt (see below TIM4_IRQHandler function)
    TIM4_Cmd(ENABLE); // TIM4 counter enable

    // IMPORTANT: this software delay is needed so timer3 work after this
    for (ui16_i = 0; ui16_i < (65000); ui16_i++) {
        ;
    }
}

// TIM4 Overflow Interrupt handler
void TIM4_IRQHandler(void) __interrupt(TIM4_OVF_IRQHANDLER) {
    ui8_tim4_counter++;
    TIM4->SR1 = 0; // Reset interrupt flag
}

