/*
 * TongSheng TSDZ2 motor controller firmware/
 *
 * Copyright (C) Casainho, 2018.
 *
 * Released under the GPL License, Version 3
 */

#include <stdint.h>
#include "stm8s_tim1.h"
#include "stm8s_flash.h"
#include "main.h"
#include "interrupts.h"
#include "pwm.h"
#include "pins.h"

void pwm_init_bipolar_4q(void) {
    // verify if PWM N channels are active on option bytes, if not, enable
    volatile uint32_t ui32_delay_counter = 0;
    // deinitialize EEPROM
    FLASH_DeInit();
    // time delay
    for (ui32_delay_counter = 0; ui32_delay_counter < 160000; ++ui32_delay_counter) {
    }
    // select and set programming time mode
    FLASH_SetProgrammingTime(FLASH_PROGRAMTIME_STANDARD); // standard programming (erase and write) time mode
    // time delay
    for (ui32_delay_counter = 0; ui32_delay_counter < 160000; ++ui32_delay_counter) {
    }

    if (FLASH_ReadOptionByte(0x4803) != 0x20) {
        FLASH_Unlock(FLASH_MEMTYPE_DATA);
        FLASH_EraseOptionByte(0x4803);
        FLASH_ProgramOptionByte(0x4803, 0x20);
        FLASH_Lock(FLASH_MEMTYPE_DATA);
    }

    TIM1_TimeBaseInit(0, // TIM1_Prescaler = 0
            TIM1_COUNTERMODE_CENTERALIGNED3,
            // clock = 16MHz; counter period = 840; PWM freq = 16MHz / 840 = 19,047kHz;
            PWM_PERIOD, // PWM center aligned mode: counts from 0 to PWM_PERIOD and then down from PWM_PERIOD to 0
            1);// will fire the TIM1_IT_UPDATE at every PWM period cycle

//#define DISABLE_PWM_CHANNELS_1_3

    TIM1_OC1Init(TIM1_OCMODE_PWM1,
#ifdef DISABLE_PWM_CHANNELS_1_3
         TIM1_OUTPUTSTATE_DISABLE,
         TIM1_OUTPUTNSTATE_DISABLE,
#else
            TIM1_OUTPUTSTATE_ENABLE,
            TIM1_OUTPUTNSTATE_ENABLE,
#endif
            255, // initial duty_cycle value
            TIM1_OCPOLARITY_HIGH,
            TIM1_OCPOLARITY_HIGH,
            TIM1_OCIDLESTATE_RESET,
            TIM1_OCNIDLESTATE_SET);

    TIM1_OC2Init(TIM1_OCMODE_PWM1,
            TIM1_OUTPUTSTATE_ENABLE,
            TIM1_OUTPUTNSTATE_ENABLE,
            255, // initial duty_cycle value
            TIM1_OCPOLARITY_HIGH,
            TIM1_OCPOLARITY_HIGH,
            TIM1_OCIDLESTATE_RESET,
            TIM1_OCIDLESTATE_SET);

    TIM1_OC3Init(TIM1_OCMODE_PWM1,
#ifdef DISABLE_PWM_CHANNELS_1_3
         TIM1_OUTPUTSTATE_DISABLE,
         TIM1_OUTPUTNSTATE_DISABLE,
#else
            TIM1_OUTPUTSTATE_ENABLE,
            TIM1_OUTPUTNSTATE_ENABLE,
#endif
            255, // initial duty_cycle value
            TIM1_OCPOLARITY_HIGH,
            TIM1_OCPOLARITY_HIGH,
            TIM1_OCIDLESTATE_RESET,
            TIM1_OCNIDLESTATE_SET);

    // OC4 is being used only to fire interrupt at a specific time (middle of both up/down TIM1 count)
    // OC4 is always syncronized with PWM
    TIM1_OC4Init(TIM1_OCMODE_PWM1,
            TIM1_OUTPUTSTATE_DISABLE,
            210,
            TIM1_OCPOLARITY_HIGH,
            TIM1_OCIDLESTATE_RESET);

    // break, dead time and lock configuration
    TIM1_BDTRConfig(TIM1_OSSISTATE_ENABLE,
            TIM1_LOCKLEVEL_OFF,
            // hardware nees a dead time of 1us
            32,// DTG = 0; dead time in 62.5 ns steps; 1.5us/62.5ns = 24
            TIM1_BREAK_DISABLE,
            TIM1_BREAKPOLARITY_LOW,
            TIM1_AUTOMATICOUTPUT_DISABLE);

    ITC_SetSoftwarePriority(ITC_IRQ_TIM1_CAPCOM, ITC_PRIORITYLEVEL_3);
    TIM1_ITConfig(TIM1_IT_CC4, ENABLE);
    TIM1_Cmd(ENABLE); // TIM1 counter enable
    TIM1_CtrlPWMOutputs(ENABLE);
}
