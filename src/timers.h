/*
 * TongSheng TSDZ2 motor controller firmware/
 *
 * Copyright (C) Casainho, 2018.
 *
 * Released under the GPL License, Version 3
 */

#ifndef TIMERS_H_
#define TIMERS_H_

#include <stdint.h>

#include <stdint.h>

void timers_init(void);

extern volatile uint8_t ui8_1ms_counter;


#endif /* TIMERS_H_ */
