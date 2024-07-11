/*
 * TongSheng TSDZ2 motor controller firmware/
 *
 * Copyright (C) Casainho, Leon, MSpider65 2020.
 *
 * Released under the GPL License, Version 3
 */

#include <stdint.h>
#include <stdio.h>
#include "stm8s.h"
#include "stm8s_adc1.h"
#include "stm8s_gpio.h"
#include "adc.h"


void adc_init(void) {
    //init GPIO for the used ADC pins
    GPIO_Init(GPIOB, (GPIO_PIN_7 | GPIO_PIN_6 | GPIO_PIN_5 | GPIO_PIN_4), GPIO_MODE_IN_FL_NO_IT);

    ADC1_DeInit();
    // Single conversion, Left alignement, Last channel = 7
    ADC1_ConversionConfig(ADC1_CONVERSIONMODE_SINGLE, ADC1_CHANNEL_7, ADC1_ALIGN_RIGHT);
    // ADC clock = CPU clock/3
    ADC1_PrescalerConfig(ADC1_PRESSEL_FCPU_D3);
    ADC1_SchmittTriggerConfig(ADC1_SCHMITTTRIG_CHANNEL4, DISABLE);
    ADC1_SchmittTriggerConfig(ADC1_SCHMITTTRIG_CHANNEL5, DISABLE);
    ADC1_SchmittTriggerConfig(ADC1_SCHMITTTRIG_CHANNEL6, DISABLE);
    ADC1_SchmittTriggerConfig(ADC1_SCHMITTTRIG_CHANNEL7, DISABLE);
    // Enable Scan mode
    ADC1_ScanModeCmd(ENABLE);
    // Conversion is started by the TRGO event generated by the TIM1 counter
    ADC1_ExternalTriggerConfig(ADC1_EXTTRIG_TIM, ENABLE);
    // ENABLE ADC1
    ADC1_Cmd(ENABLE);
}
