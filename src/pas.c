/*
 * TongSheng TSDZ2 motor controller firmware/
 *
 * Copyright (C) Casainho, 2018.
 *
 * Released under the GPL License, Version 3
 */

#include "stm8s_gpio.h"
#include "pins.h"

void pas_init(void) {
    //PAS1 pin as external input pin
    GPIO_Init(PAS1__PORT, PAS1__PIN, GPIO_MODE_IN_PU_NO_IT); // input pull-up, no external interrupt

    //PAS2 pin as external input pin interrupt
    GPIO_Init(PAS2__PORT, PAS2__PIN, GPIO_MODE_IN_PU_NO_IT); // input pull-up, no external interrupt
}
