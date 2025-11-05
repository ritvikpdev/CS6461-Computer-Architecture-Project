; IMPORTANT: Addresses 000000..000005 are machine-reserved. We set LOC 6.
; This is the "Closest Number Finder" (Program 1)
; required by the assembler.
LOC 6
JMA 0, 0, START

; --- Subroutine: READ_TARGET ---
; Reads a multi-digit character string from IN, parses it to an integer,
; and stores the result in the TARGET variable.
READ_TARGET:
LDR 0, 0, ZERO    ; result
LDR 1, 0, ZERO    ; signFlag (1 if '-')
LDR 2, 0, ZERO    ; seenDigit
RT_LOOP:
IN 3, 0           ; Read one character
STR 3, 0, READBUF ; Store char
; If no digit seen, allow leading '-'
JZ 2, 0, RT_CHECK_SIGN
RT_DIGIT_CHECK:
LDR 3, 0, READBUF
SMR 3, 0, ASCII_0 ; R3 = char - '0'
JGE 3, 0, RT_GE_ZERO ; If (char >= '0'), it might be a digit
; Not a digit: if no digit yet, keep scanning; else finalize
JZ 2, 0, RT_LOOP
JMA 0, 0, RT_DONE
RT_CHECK_SIGN:
LDR 3, 0, READBUF
SMR 3, 0, ASCII_MINUS ; R3 = char - '-'
JZ 3, 0, RT_SET_NEG   ; If (char == '-'), set sign flag
JMA 0, 0, RT_DIGIT_CHECK
RT_SET_NEG:
LDR 1, 0, ZERO
AIR 1, 1          ; signFlag = 1
JMA 0, 0, RT_LOOP
RT_GE_ZERO:
SIR 3, 10         ; R3 = (char - '0') - 10
JGE 3, 0, RT_NOT_DIGIT ; If (R3 >= 0), then char > '9'
AIR 3, 10         ; R3 = (char - '0') -> the digit value
; seenDigit = 1
LDR 2, 0, ZERO
AIR 2, 1
; Accumulate: R0 = R010 + digit (x10 via 8+2)
STR 3, 0, PRINT_TMP ; save digit
STR 0, 0, READBUF   ; save R0
LDR 0, 0, READBUF
SRC 0, 1, 1, 1    ; R0 = R0 * 2
STR 0, 0, TEMP_DIFF ; TEMP_DIFF = R0 * 2
LDR 0, 0, READBUF
SRC 0, 3, 1, 1    ; R0 = R0 * 8
AMR 0, 0, TEMP_DIFF ; R0 = (R08) + (R02) = R010
AMR 0, 0, PRINT_TMP ; R0 = (R0*10) + digit
JMA 0, 0, RT_LOOP
RT_NOT_DIGIT:
JZ 2, 0, RT_LOOP ; Not a digit, but haven't seen one yet
RT_DONE:
; Apply sign
JZ 1, 0, RT_POS ; If signFlag == 0, jump to end
NOT 0             ; Two's complement
AIR 0, 1
RT_POS:
STR 0, 0, TARGET  ; Store final result
JMA 0, 0, AFTER_TARGET ; Return to main flow

; --- Subroutine: READ_CAND ---
; Reads a candidate number. Stores result in READVAL.
; Jumps to AFTER_CAND_FIRST if MODE=1, or AFTER_CAND_LOOP if MODE=0
READ_CAND:
LDR 0, 0, ZERO    ; result
LDR 1, 0, ZERO    ; signFlag
LDR 2, 0, ZERO    ; seenDigit
RC_LOOP:
IN 3, 0
STR 3, 0, READBUF
JZ 2, 0, RC_CHECK_SIGN
RC_DIGIT_CHECK:
LDR 3, 0, READBUF
SMR 3, 0, ASCII_0
JGE 3, 0, RC_GE_ZERO
JZ 2, 0, RC_LOOP
JMA 0, 0, RC_DONE
RC_CHECK_SIGN:
LDR 3, 0, READBUF
SMR 3, 0, ASCII_MINUS
JZ 3, 0, RC_SET_NEG
JMA 0, 0, RC_DIGIT_CHECK
RC_SET_NEG:
LDR 1, 0, ZERO
AIR 1, 1
JMA 0, 0, RC_LOOP
RC_GE_ZERO:
SIR 3, 10
JGE 3, 0, RC_NOT_DIGIT
AIR 3, 10
LDR 2, 0, ZERO
AIR 2, 1
STR 3, 0, PRINT_TMP
STR 0, 0, READBUF
LDR 0, 0, READBUF
SRC 0, 1, 1, 1
STR 0, 0, TEMP_DIFF
LDR 0, 0, READBUF
SRC 0, 3, 1, 1
AMR 0, 0, TEMP_DIFF
AMR 0, 0, PRINT_TMP
JMA 0, 0, RC_LOOP
RC_NOT_DIGIT:
JZ 2, 0, RC_LOOP
RC_DONE:
JZ 1, 0, RC_POS
NOT 0
AIR 0, 1
RC_POS:
STR 0, 0, READVAL
; Dispatch return based on MODE: 1 -> first-candidate init, 0 -> loop
LDR 2, 0, MODE
JZ 2, 0, AFTER_CAND_LOOP
; MODE==1 -> clear MODE and go to first-candidate return
LDR 2, 0, ZERO
STR 2, 0, MODE
JMA 0, 0, AFTER_CAND_FIRST

; === Main ===
START:
; Read TARGET first
JMA 0, 0, READ_TARGET
AFTER_TARGET:
; Read first candidate -> initialize WINNER/MIN_DIFF
; Indicate first-candidate return path via MODE=1
LDR 2, 0, ZERO
AIR 2, 1
STR 2, 0, MODE
JMA 0, 0, READ_CAND
AFTER_CAND_FIRST:
LDR 0, 0, READVAL
STR 0, 0, WINNER
LDR 0, 0, WINNER
SMR 0, 0, TARGET
JGE 0, 0, MINPOS0
NOT 0
AIR 0, 1
MINPOS0:
STR 0, 0, MIN_DIFF
; Set CNT = 19 remaining (since we already processed 1st)
LDR 2, 0, ZERO
AIR 2, 19
STR 2, 0, CNT
; Loop remaining 19 candidates
CAND_LOOP:
LDR 2, 0, CNT
JZ 2, 0, COMP_DONE
SIR 2, 1
STR 2, 0, CNT
; Ensure MODE=0 for loop returns
LDR 2, 0, ZERO
STR 2, 0, MODE
JMA 0, 0, READ_CAND
AFTER_CAND_LOOP:
LDR 0, 0, READVAL
STR 0, 0, PRINT_TMP ; PRINT_TMP holds current candidate
; diff = |cand - TARGET|
LDR 0, 0, PRINT_TMP
SMR 0, 0, TARGET
JGE 0, 0, DIFFPOS
NOT 0
AIR 0, 1
DIFFPOS:
STR 0, 0, TEMP_DIFF
; if diff < MIN_DIFF -> update MIN_DIFF and WINNER
LDR 1, 0, MIN_DIFF
LDR 2, 0, TEMP_DIFF
SMR 2, 0, MIN_DIFF  ; R2 = new_diff - min_diff
JGE 2, 0, NO_UPD  ; if (new_diff >= min_diff), skip
; Update MIN_DIFF and WINNER (PRINT_TMP still holds this candidate)
LDR 2, 0, TEMP_DIFF
STR 2, 0, MIN_DIFF
LDR 3, 0, PRINT_TMP
STR 3, 0, WINNER
NO_UPD:
JMA 0, 0, CAND_LOOP

; --- Subroutine: Print WINNER as multi-digit decimal with sign ---
COMP_DONE:
; R1 holds |WINNER|, R2=pow, R0 temp
LDR 0, 0, WINNER
JGE 0, 0, PW_ABS
LDR 3, 0, ASCII_MINUS ; Load '-'
OUT 3, 1          ; Print '-'
LDR 1, 0, WINNER
NOT 1
AIR 1, 1
JMA 0, 0, PW_ZCHK
PW_ABS:
LDR 1, 0, WINNER ; R1 = abs(WINNER)
PW_ZCHK:
SMR 1, 0, ZERO
JNE 1, 0, PW_FINDPOW
LDR 3, 0, ASCII_0 ; R1 == 0, so just print '0'
OUT 3, 1
HLT

; Find highest power of 10
PW_FINDPOW:
LDR 2, 0, ZERO
AIR 2, 1          ; R2 (pow) = 1
STR 1, 0, READBUF ; READBUF = R1 (abs_val)
PW_POW_LOOP:
LDR 0, 0, READBUF ; R0 = abs_val
SIR 0, 10         ; R0 = abs_val - 10
JGE 0, 0, PW_POW_STEP
JMA 0, 0, PW_PRINT ; abs_val < 10, so pow is correct
PW_POW_STEP:
LDR 0, 0, READBUF
LDR 3, 0, ZERO
AIR 3, 10
DVD 0, 3          ; R0 = abs_val / 10
STR 0, 0, READBUF ; abs_val = R0
; R2 = R2 * 10
STR 2, 0, PRINT_TMP
LDR 0, 0, PRINT_TMP
SRC 0, 1, 1, 1
STR 0, 0, TEMP_DIFF
LDR 0, 0, PRINT_TMP
SRC 0, 3, 1, 1
AMR 0, 0, TEMP_DIFF
STR 0, 0, PRINT_TMP
LDR 2, 0, PRINT_TMP
JMA 0, 0, PW_POW_LOOP

; Print loop:
PW_PRINT:
; R2 still holds the correct power from PW_FINDPOW.
PW_PRINT_LOOP:
STR 2, 0, PRINT_TMP ; save pow
STR 1, 0, READBUF   ; save R1 (remainder)
LDR 0, 0, READBUF
LDR 2, 0, PRINT_TMP
DVD 0, 2          ; R0=digit, R2=new_remainder
STR 0, 0, TEMP_DIFF ; save digit
STR 2, 0, READBUF   ; save new_remainder
LDR 1, 0, READBUF   ; R1 = new_remainder
; pow = pow / 10
LDR 2, 0, PRINT_TMP ; reload old pow
LDR 0, 0, ZERO
AIR 0, 10
DVD 2, 0          ; R2 = pow / 10
; print the saved digit
LDR 0, 0, TEMP_DIFF
STR 0, 0, PRINT_TMP
LDR 3, 0, ASCII_0
AMR 3, 0, PRINT_TMP
OUT 3, 1
; Continue if pow != 0
SMR 2, 0, ZERO
JNE 2, 0, PW_PRINT_LOOP
HLT

; --- Data ---
LOC 239
ASCII_0:     DATA 48
ASCII_MINUS: DATA 45
TEMP_DIFF:   DATA 0
MIN_DIFF:    DATA 0
WINNER:      DATA 0
CNT:         DATA 0
TARGET:      DATA 0
READBUF:     DATA 0
PRINT_TMP:   DATA 0
ZERO:        DATA 0
READVAL:     DATA 0
LINK_SAVE:   DATA 0
MODE:        DATA 0

END