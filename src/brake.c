/*
 * TongSheng TSDZ2 motor controller firmware/
 *
 * Copyright (C) Casainho, MSpider65 2020.
 *
 * Released under the GPL License, Version 3
 */

#include "stm8s_gpio.h"
#include "pins.h"
#include "brake.h"

void brake_init(void) {
    // brake pin as external input pin interrupt
    GPIO_Init(BRAKE__PORT, BRAKE__PIN, GPIO_MODE_IN_FL_NO_IT); // with external interrupt
}

