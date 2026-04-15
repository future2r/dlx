; Example DLX Program

        .data       ; data section
a:      .word 10    ; operand 1
b:      .word 32    ; operand 2
res:    .word 0     ; result

        .text           ; program section
main:
        lw r2, a(r0)    ; load 'a' into R2
        lw r3, b(r0)    ; load 'b' into R3
        add r4, r2, r3  ; R2 + R3 = R4
        sw res(r0), r4  ; store R4 into 'res'
        trap 0          ; end program