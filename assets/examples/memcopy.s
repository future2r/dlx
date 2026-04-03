; Memory copy
; Copies a null-terminated string from src to dst byte by byte.

        .data
src:    .asciiz "DLX Simulator"   ; source string (13 bytes)
        .align 4                  ; align at 16 bytes to look nicer
dst:    .space 16                 ; destination buffer (16 bytes)

        .text
        addi    r1, r0, src       ; r1 = source pointer
        addi    r2, r0, dst       ; r2 = destination pointer

loop:   lb      r3, 0(r1)         ; load byte from source
        sb      0(r2), r3         ; store byte to destination
        beqz    r3, done          ; null terminator -> finished
        addi    r1, r1, 1         ; advance source pointer
        addi    r2, r2, 1         ; advance destination pointer
        j       loop              ; repeat

done:   halt
