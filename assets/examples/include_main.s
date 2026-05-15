; Demonstrates the .include directive: pulls the "msg" label
; in from include_utils.s and prints it.

        .include "include_utils.s"

        .text
main:   addi r4, r0, msg
        trap 3
        trap 0
