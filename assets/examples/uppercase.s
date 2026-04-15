; Convert string to upper case

        .data
str:    .asciiz "Hello World!"    ; null-terminated string to convert

        .text
        addi    r4, r0, str       ; r4 = address of string
	trap    3                 ; print the original text

loop:   lb      r2, 0(r4)         ; load current character
        beqz    r2, done          ; null terminator → end of string
        slti    r3, r2, 97        ; r3 = 1 if r2 < 'a'
        bnez    r3, skip          ; not a lowercase letter, skip
        slti    r3, r2, 123       ; r3 = 1 if r2 ≤ 'z'
        beqz    r3, skip          ; not a lowercase letter, skip
        subi    r2, r2, 32        ; subtract 32: 'a'→'A', 'b'→'B', …
        sb      0(r4), r2         ; store uppercase character back
skip:   addi    r4, r4, 1         ; advance pointer to next character
        j       loop              ; repeat

done:   addi    r4, r0, str       ; r4 = address of string
        trap    3                 ; print the converted text
        trap    0                 ; end program
