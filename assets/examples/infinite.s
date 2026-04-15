; Infinite counter
; Increments a memory word in an endless loop.

        .data
count:  .word 0                   ; counter variable

        .text
loop:   lw      r2, count(r0)     ; load current count
        addi    r2, r2, 1         ; increment
        sw      count(r0), r2     ; store back
        j       loop              ; repeat forever
