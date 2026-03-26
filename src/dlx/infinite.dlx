; Infinite counter
; Increments a memory word in an endless loop.

        .data
count:  .word 0                   ; counter variable

        .text
loop:   lw      r1, count(r0)     ; load current count
        addi    r1, r1, 1         ; increment
        sw      count(r0), r1     ; store back
        j       loop              ; repeat forever
