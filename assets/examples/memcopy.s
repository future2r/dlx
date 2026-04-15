; Memory copy
; Copies a null-terminated string from src to dst byte by byte.

        .data
src:    .asciiz "DLX Simulator"   ; source string (13 bytes)
        .align 4                  ; align at 16 bytes to look nicer
dst:    .space 16                 ; destination buffer (16 bytes)

        .text
        addi    r2, r0, src       ; r2 = source pointer
        addi    r3, r0, dst       ; r3 = destination pointer

loop:   lb      r4, 0(r2)         ; load byte from source
        sb      0(r3), r4         ; store byte to destination
        beqz    r4, done          ; null terminator -> finished
        addi    r2, r2, 1         ; advance source pointer
        addi    r3, r3, 1         ; advance destination pointer
        j       loop              ; repeat

done:   trap	0
