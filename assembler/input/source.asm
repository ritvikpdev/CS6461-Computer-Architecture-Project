LOC 6                   ; Begin Program Code at Address 6

; ==========================================================
; INITIALIZATION SECTION
; ==========================================================
INIT:
LDX 1, PARA_PTR     ; X1 points to start of Paragraph
LDA 0, 0, 0         ; Clear R0

; ==========================================================
; PART 1: PRINT THE PARAGRAPH
; ==========================================================
PRINT_LOOP:
LDR 0, 1, 0         ; Load char from Paragraph (X1) into R0
JZ  0, 0, INPUT_INIT ; If char is 0 (NULL terminator), done printing
OUT 0, 1            ; Output char to Printer (DevID 1)

; Increment X1 (Paragraph Pointer)
; Logic: Store X1 -> Load to GPR -> Add 1 -> Store back -> Load X1
STX 1, TEMP_X
LDR 3, 0, TEMP_X
AIR 3, 1
STR 3, 0, TEMP_X
LDX 1, TEMP_X

JMA 0, PRINT_LOOP   ; Loop back


; ==========================================================
; PART 2: ASK USER FOR SEARCH WORD
; ==========================================================
INPUT_INIT:
LDX 2, WORD_PTR     ; X2 points to Search Word Buffer

; Print "Enter Word: " (ASCII codes omitted for brevity, implied functionality)


READ_LOOP:
IN 0, 0             ; Input from Keyboard (DevID 0) to R0
OUT 0, 1            ; Echo input to Printer

; Check for Enter Key (Newline = 13 or 10)
STR 0, 0, TEMP_CHAR ; Store input to temp for comparison
LDX 3, ENTER_KEY    ; Load address of Enter Key constant
SMR 0, 3, 0         ; R0 = R0 - 13
JZ  0, 0, SEARCH_SETUP ; If Enter pressed, start search

; Store char in buffer
LDR 0, 0, TEMP_CHAR ; Reload original char
STR 0, 2, 0         ; Store R0 into Word Buffer (X2)

; Increment X2 (Word Buffer Pointer)
STX 2, TEMP_X
LDR 3, 0, TEMP_X
AIR 3, 1
STR 3, 0, TEMP_X
LDX 2, TEMP_X

JMA 0, READ_LOOP    ; Continue reading


; ==========================================================
; PART 3: SEARCH LOGIC
; ==========================================================
SEARCH_SETUP:
; Terminate the Search Word buffer with NULL
LDA 0, 0, 0
STR 0, 2, 0

; Reset Pointers and Counters
LDX 1, PARA_PTR     ; X1 = Paragraph Start
LDA 2, 1, 0         ; R2 = Sentence Counter (Start at 1)
LDA 3, 1, 0         ; R3 = Word Counter (Start at 1)


SEARCH_MAIN_LOOP:
LDR 0, 1, 0         ; Load Paragraph Char
JZ  0, 0, NOT_FOUND ; If NULL, end of text, word not found

; --- Update Counters ---

; Check for Period (Sentence End)
STR 0, 0, TEMP_CHAR
LDX 0, PERIOD_KEY   ; Load Period Constant Address
LDR 1, 0, TEMP_CHAR ; Reload char
SMR 1, 0, 0         ; Compare
JZ  1, 0, INC_SENT  ; If '.', go to increment sentence

; Check for Space (Word End)
LDR 1, 0, TEMP_CHAR
LDX 0, SPACE_KEY
SMR 1, 0, 0
JZ  1, 0, INC_WORD  ; If ' ', go to increment word

; --- Check for Word Match ---
; Compare current Para char with FIRST char of Search Word
LDR 1, 0, TEMP_CHAR
LDX 2, WORD_PTR     ; X2 = Start of Search Word
LDR 0, 2, 0         ; Load 1st char of Search Word

; Pseudo-Compare: R1 (Para) - R0 (Word)
STR 0, 0, TEMP_COMP ; Store Word char to temp
SMR 1, 0, TEMP_COMP ; Subtract
JZ  1, 0, CHECK_FULL ; If first char matches, check full word

JMA 0, NEXT_CHAR    ; Otherwise, move to next char


INC_SENT:
AIR 2, 1            ; Sentence Count ++
LDA 3, 1, 0         ; Reset Word Count to 1
JMA 0, NEXT_CHAR

INC_WORD:
AIR 3, 1            ; Word Count ++
JMA 0, NEXT_CHAR

; ==========================================================
; CHECK FULL WORD SUBROUTINE (Unrolled Logic)
; ==========================================================
CHECK_FULL:
; We are here because Para[X1] matched Word[0].
; Now check Para[X1+1] == Word[1], etc.
; Save current X1 so we don't lose our place if it fails
STX 1, SAVE_X1
STX 2, SAVE_X2      ; X2 is currently at Word start

CHECK_LOOP:
; Inc X1 and X2 to check next chars
; (Increment X1)
STX 1, TEMP_X
LDR 0, 0, TEMP_X
AIR 0, 1
STR 0, 0, TEMP_X
LDX 1, TEMP_X

; (Increment X2)
STX 2, TEMP_X
LDR 0, 0, TEMP_X
AIR 0, 1
STR 0, 0, TEMP_X
LDX 2, TEMP_X

; Load chars
LDR 0, 1, 0         ; Next Para Char
LDR 1, 2, 0         ; Next Word Char

; If Word Char is NULL, we reached end of search word -> MATCH SUCCESS!
JZ  1, 0, FOUND_MATCH

; If Para Char is NULL but Word isn't -> Fail
JZ  0, 0, RESET_SEARCH

; Compare
STR 1, 0, TEMP_COMP
SMR 0, 0, TEMP_COMP
JZ  0, 0, CHECK_LOOP ; If equal, keep checking

; If not equal, fall through to RESET_SEARCH


RESET_SEARCH:
LDX 1, SAVE_X1      ; Restore X1 to where we started checking
LDX 2, WORD_PTR     ; Restore X2
JMA 0, NEXT_CHAR

; ==========================================================
; NEXT CHAR & FOUND LOGIC
; ==========================================================
NEXT_CHAR:
; Increment X1 Main Pointer
STX 1, TEMP_X
LDR 0, 0, TEMP_X
AIR 0, 1
STR 0, 0, TEMP_X
LDX 1, TEMP_X
JMA 0, SEARCH_MAIN_LOOP

FOUND_MATCH:
; Print "FOUND: "
LDX 2, STR_FOUND
JSR 0, PRINT_STR

; Print Search Word (from Buffer)
LDX 2, WORD_PTR
JSR 0, PRINT_STR

; Print " SENTENCE: "
LDX 2, STR_SENT
JSR 0, PRINT_STR

; Print Sentence Count (R2) - Needs conversion to ASCII
; (Simplified: Assuming single digit 0-9. Add 48 to convert to ASCII)
AIR 2, 48
OUT 2, 1

; Print " WORD: "
LDX 2, STR_WORD
JSR 0, PRINT_STR

; Print Word Count (R3)
AIR 3, 48
OUT 3, 1

HLT                 ; End Program


NOT_FOUND:
LDX 2, STR_NOTFOUND
JSR 0, PRINT_STR
HLT

; ==========================================================
; UTILS & DATA
; ==========================================================
PRINT_STR:
; Prints string pointed to by X2
LDR 0, 2, 0
JZ  0, 0, RFS_RET
OUT 0, 1
; Inc X2
STX 2, TEMP_X2
LDR 0, 0, TEMP_X2
AIR 0, 1
STR 0, 0, TEMP_X2
LDX 2, TEMP_X2
JMA 0, PRINT_STR
RFS_RET:
RFS 0               ; Return from Subroutine (R0=Link)

; DATA STORAGE
TEMP_X:      Data 0
TEMP_X2:     Data 0
TEMP_CHAR:   Data 0
TEMP_COMP:   Data 0
SAVE_X1:     Data 0
SAVE_X2:     Data 0

ENTER_KEY:   Data 13    ; CR
PERIOD_KEY:  Data 46    ; .
SPACE_KEY:   Data 32    ; Space

PARA_PTR:    Data 1000
WORD_PTR:    Data 2000

STR_FOUND:   Data 70, 79, 85, 78, 68, 58, 32, 0         ; "FOUND: "
STR_SENT:    Data 32, 83, 69, 78, 84, 58, 32, 0         ; " SENT: "
STR_WORD:    Data 32, 87, 79, 82, 68, 58, 32, 0         ; " WORD: "
STR_NOTFOUND: Data 78, 79, 84, 32, 70, 79, 85, 78, 68, 0 ; "NOT FOUND"

; Pointers for strings
LOC 1000
; "The cat. The dog. It ran. Sun hot. Sky blue. The End."
Data 84, 104, 101, 32, 99, 97, 116, 46, 32, 84, 104, 101, 32, 100, 111, 103, 46, 32, 73, 116, 32, 114, 97, 110, 46, 32, 83, 117, 110, 32, 104, 111, 116, 46, 32, 83, 107, 121, 32, 98, 108, 117, 101, 46, 32, 84, 104, 101, 32, 69, 110, 100, 46, 0

LOC 2000
Data 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 ; Reserve space for input