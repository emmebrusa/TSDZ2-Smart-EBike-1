/*
 * TongSheng TSDZ2 motor controller firmware/
 *
 * Copyright (C) Casainho, MSpider65 2020.
 *
 * Released under the GPL License, Version 3
 */

#ifndef UART_H_
#define UART_H_

#include <stdint.h>

void uart2_init(void);

int uart_put_char(int c);

int uart_get_char(void);

void uart2_send_buffer_start(void);

#endif /* UART_H_ */

