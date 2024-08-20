/*
 * TongSheng TSDZ2 motor controller firmware/
 *
 * Copyright (C) Casainho, MSpider65 2020.
 *
 * Released under the GPL License, Version 3
 */

#include <stdint.h>

#include "stm8s.h"
#include "stm8s_uart2.h"
#include "main.h"
#include "interrupts.h"


void uart2_init(void)
{
    UART2_DeInit();

    UART2_Init((uint32_t) 9600,
            UART2_WORDLENGTH_8D,
            UART2_STOPBITS_1,
            UART2_PARITY_NO,
            UART2_SYNCMODE_CLOCK_DISABLE,
            UART2_MODE_TXRX_ENABLE);

    UART2_ITConfig(UART2_IT_RXNE_OR, ENABLE);
	
	// Set UART2 IRQ priority to level 1 :0=lowest - 3=highest(default value)
    ITC_SetSoftwarePriority(UART2_IRQHANDLER, ITC_PRIORITYLEVEL_2);
}

int uart_put_char(int c)
{
  //Write a character to the UART2
  UART2_SendData8(c);

  //Loop until the end of transmission
  while (UART2_GetFlagStatus(UART2_FLAG_TXE) == RESET);

  return((unsigned char)c);
}

int uart_get_char(void)
{
  uint8_t c = 0;

  /* Loop until the Read data register flag is SET */
  while (UART2_GetFlagStatus(UART2_FLAG_RXNE) == RESET) ;

  c = UART2_ReceiveData8();

  return (c);
}
