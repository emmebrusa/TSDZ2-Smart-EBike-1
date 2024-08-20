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
#include "pwm.h"

void pwm_init(void) {
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
            TIM1_COUNTERMODE_CENTERALIGNED3,  // Compare interrupt is fired twice (when counter is counting up and down)
            // clock = 16MHz; counter period = 840; PWM freq = 16MHz / 840 = 19,047kHz;
            PWM_COUNTER_MAX, // PWM center aligned mode: counts up from 0 to PWM_COUNTER_MAX and then down again to 0
            1);// will fire the TIM1_IT_UPDATE at every PWM period cycle

    TIM1_OC1Init(TIM1_OCMODE_PWM1,
            TIM1_OUTPUTSTATE_ENABLE,
            TIM1_OUTPUTNSTATE_ENABLE,
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
            TIM1_OUTPUTSTATE_ENABLE,
            TIM1_OUTPUTNSTATE_ENABLE,
            255, // initial duty_cycle value
            TIM1_OCPOLARITY_HIGH,
            TIM1_OCPOLARITY_HIGH,
            TIM1_OCIDLESTATE_RESET,
            TIM1_OCNIDLESTATE_SET);

    // OC4 is being used only to fire interrupt at a specific time (middle of both up/down TIM1 count)
    TIM1_OC4Init(TIM1_OCMODE_PWM1,
            TIM1_OUTPUTSTATE_DISABLE,
            PWM_COUNTER_MAX/2,
            TIM1_OCPOLARITY_HIGH,
            TIM1_OCIDLESTATE_RESET);

    // break, dead time and lock configuration
    TIM1_BDTRConfig(TIM1_OSSISTATE_ENABLE,
            TIM1_LOCKLEVEL_OFF,
            // hardware nees a dead time of 1us
            24,// dead time in 62.5 ns steps; 1.5us/62.5ns = 24 (Value used: OEM Firmware=49 - OSF Firmware=16)
            TIM1_BREAK_DISABLE,
            TIM1_BREAKPOLARITY_LOW,
            TIM1_AUTOMATICOUTPUT_DISABLE);

    // Select OC4REF signal as trigger output (TRGO)
    // ADC conversion is started on the rising edge of TRGO signal
    // In TIM1_OCMODE_PWM1 mode, OC4REF is high between 210 down counting and 210 up counting of TIM1 counter
    // -> The rising edge of TRGO is generated when TIM1 is down counting and the value is 210.
    // Battery current is on ADC channel 5 and in scan mode the value is sampled
    // after 3(ADC Prescaler)*14(ADC Clocks per conversion)*5(channel nr)=210 CPU clock cycles
    // This means the battery current is sampled exactly in the middle of PWM cycle (TIM1 counter = 0)
    TIM1->CR2 = (uint8_t)((uint8_t)(TIM1->CR2 | ((uint8_t) 0x70)));

    // TIM1 IRQ priority = 2. Priority increases from 1 (min priority) to 3 (max priority)
    ITC_SetSoftwarePriority(ITC_IRQ_TIM1_CAPCOM, ITC_PRIORITYLEVEL_2);
    // Set TIM1 interrupt on OC4 Compare
    TIM1_ITConfig(TIM1_IT_CC4, ENABLE);
    // enable TIM1 counter
    TIM1_Cmd(ENABLE);
    TIM1_CtrlPWMOutputs(ENABLE);
}
