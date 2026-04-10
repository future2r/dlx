; Example DLX Program

        .data       ; data section
a:      .word 10    ; operand 1
b:      .word 32    ; operand 2
res:    .word 0     ; result

        .text           ; program section
main:
        lw r1, a(r0)    ; load 'a' into R1
        lw r2, b(r0)    ; load 'b' into R2
        add r3, r1, r2  ; R1 + R2 = R3 
        sw res(r0), r3  ; store R3 into 'res'
        trap 0          ; end program