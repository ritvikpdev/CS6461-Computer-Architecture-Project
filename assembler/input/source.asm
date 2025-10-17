LOC 50
JZ  1,0,100     ; Jump if R1 == 0
JNE 2,1,120     ; Jump if R2 != 0, indexed by IX1
JCC 0,0,130     ; Jump if condition code set
JMA 140         ; Unconditional jump
JSR 150         ; Jump to subroutine
RFS 3           ; Return from subroutine with return code 3
SOB 3,0,160     ; Decrement R3 and branch if > 0
JGE 2,0,170     ; Jump if R2 >= 0
END
