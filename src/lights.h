/*
 * TongSheng TSDZ2 motor controller firmware/
 *
 * Copyright (C) Casainho, 2018.
 *
 * Released under the GPL License, Version 3
 */

#ifndef LIGHTS_H_
#define LIGHTS_H_

#include <stdint.h>

void lights_init(void);
void lights_set_state(uint8_t ui8_state);

#endif /* _LIGHTS_H_ */
