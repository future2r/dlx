; Hello World!

        .data       
msg:    .asciiz "Hello World!\n"

        .text
main:   addi r1, r0, msg
        trap 3
        trap 0