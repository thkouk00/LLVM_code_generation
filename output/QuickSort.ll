@.QuickSort_vtable = global [0 x i8*] []
@.QS_vtable = global [4 x i8*] [i8* bitcast (i32 (i8*, i32)* @QS.Start to i8*), i8* bitcast (i32 (i8*, i32, i32)* @QS.Sort to i8*), i8* bitcast (i32 (i8*)* @QS.Print to i8*), i8* bitcast (i32 (i8*, i32)* @QS.Init to i8*)]

declare i8* @calloc(i32, i32)
declare i32 @printf(i8*, ...)
declare void @exit(i32)

@_cint = constant [4 x i8] c"%d\0a\00"
@_cOOB = constant [15 x i8] c"Out of bounds\0a\00"
define void @print_int(i32 %i) {
	%_str = bitcast [4 x i8]* @_cint to i8*
	call i32 (i8*, ...) @printf(i8* %_str, i32 %i)
	ret void
}

define void @throw_oob() {
	%_str = bitcast [15 x i8]* @_cOOB to i8*
	call i32 (i8*, ...) @printf(i8* %_str)
	call void @exit(i32 1)
	ret void
}

define i32 @main() {
	%_0 = call i8* @calloc(i32 1, i32 20)
	%_1 = bitcast i8* %_0 to i8***
	%_2 = getelementptr [4 x i8*], [4 x i8*]* @.QS_vtable, i32 0, i32 0
	store i8** %_2, i8*** %_1
	; QS.Start : 0
	%_3 = bitcast i8* %_0 to i8***
	%_4 = load i8**, i8*** %_3
	%_5 = getelementptr i8*, i8** %_4, i32 0
	%_6 = load i8*, i8** %_5
	%_7 = bitcast i8* %_6 to i32 (i8*, i32)*
	%_8 = call i32 %_7(i8* %_0, i32 10)
	call void (i32) @print_int(i32 %_8)


	ret i32 0
}

define i32 @QS.Start(i8* %this, i32 %.sz) {
	%sz = alloca i32
	store i32 %.sz, i32* %sz
	%aux01 = alloca i32
	; QS.Init : 3
	%_0 = bitcast i8* %this to i8***
	%_1 = load i8**, i8*** %_0
	%_2 = getelementptr i8*, i8** %_1, i32 3
	%_3 = load i8*, i8** %_2
	%_4 = bitcast i8* %_3 to i32 (i8*, i32)*
	%_6 = load i32, i32* %sz
	%_5 = call i32 %_4(i8* %this, i32 %_6)
	store i32 %_5, i32* %aux01
	; QS.Print : 2
	%_7 = bitcast i8* %this to i8***
	%_8 = load i8**, i8*** %_7
	%_9 = getelementptr i8*, i8** %_8, i32 2
	%_10 = load i8*, i8** %_9
	%_11 = bitcast i8* %_10 to i32 (i8*)*
	%_12 = call i32 %_11(i8* %this)
	store i32 %_12, i32* %aux01
	call void (i32) @print_int(i32 9999)
	%_13 = getelementptr i8, i8* %this, i32 16
	%_14 = bitcast i8* %_13 to i32*
	%_15 = load i32, i32* %_14
	%_16 = sub i32 %_15, 1
	store i32 %_16, i32* %aux01
	; QS.Sort : 1
	%_17 = bitcast i8* %this to i8***
	%_18 = load i8**, i8*** %_17
	%_19 = getelementptr i8*, i8** %_18, i32 1
	%_20 = load i8*, i8** %_19
	%_21 = bitcast i8* %_20 to i32 (i8*, i32, i32)*
	%_23 = load i32, i32* %aux01
	%_22 = call i32 %_21(i8* %this, i32 0, i32 %_23)
	store i32 %_22, i32* %aux01
	; QS.Print : 2
	%_24 = bitcast i8* %this to i8***
	%_25 = load i8**, i8*** %_24
	%_26 = getelementptr i8*, i8** %_25, i32 2
	%_27 = load i8*, i8** %_26
	%_28 = bitcast i8* %_27 to i32 (i8*)*
	%_29 = call i32 %_28(i8* %this)
	store i32 %_29, i32* %aux01
	ret i32 0
}

define i32 @QS.Sort(i8* %this, i32 %.left, i32 %.right) {
	%left = alloca i32
	store i32 %.left, i32* %left
	%right = alloca i32
	store i32 %.right, i32* %right
	%v = alloca i32
	%i = alloca i32
	%j = alloca i32
	%nt = alloca i32
	%t = alloca i32
	%cont01 = alloca i1
	%cont02 = alloca i1
	%aux03 = alloca i32
	store i32 0, i32* %t
	%_3 = load i32, i32* %left
	%_4 = load i32, i32* %right
	%_5 = icmp slt i32 %_3, %_4
	br i1 %_5, label %if0, label %if1

if0: 
	%_6 = getelementptr i8, i8* %this, i32 8
	%_7 = bitcast i8* %_6 to i32**
	%_8 = load i32*, i32** %_7
	%_9 = load i32, i32* %right
	%_10 = load i32, i32* %_8
	%_11 = icmp ult i32 %_9, %_10
	br i1 %_11, label %oob15, label %oob16
	oob15:
	%_12 = add i32 %_9, 1
	%_13 = getelementptr i32, i32* %_8, i32 %_12
	%_14 = load i32, i32* %_13
	br label %oob17
	oob16:
	call void @throw_oob()
	br label %oob17
	oob17:
	store i32 %_14, i32* %v
	%_18 = load i32, i32* %left
	%_19 = sub i32 %_18, 1
	store i32 %_19, i32* %i
	%_20 = load i32, i32* %right
	store i32 %_20, i32* %j
	store i1 1, i1* %cont01
	br label %loop21
	loop21:
	%_24 = load i1, i1* %cont01
	br i1 %_24, label %loop22, label %loop23

	loop22:
	store i1 1, i1* %cont02
	br label %loop25
	loop25:
	%_28 = load i1, i1* %cont02
	br i1 %_28, label %loop26, label %loop27

	loop26:
	%_29 = load i32, i32* %i
	%_30 = add i32 %_29, 1
	store i32 %_30, i32* %i
	%_31 = getelementptr i8, i8* %this, i32 8
	%_32 = bitcast i8* %_31 to i32**
	%_33 = load i32*, i32** %_32
	%_34 = load i32, i32* %i
	%_35 = load i32, i32* %_33
	%_36 = icmp ult i32 %_34, %_35
	br i1 %_36, label %oob40, label %oob41
	oob40:
	%_37 = add i32 %_34, 1
	%_38 = getelementptr i32, i32* %_33, i32 %_37
	%_39 = load i32, i32* %_38
	br label %oob42
	oob41:
	call void @throw_oob()
	br label %oob42
	oob42:
	store i32 %_39, i32* %aux03
	%_46 = load i32, i32* %aux03
	%_47 = load i32, i32* %v
	%_48 = icmp slt i32 %_46, %_47
	%_49 = xor i1 1, %_48
	br i1 %_49, label %if43, label %if44

if43: 
	store i1 0, i1* %cont02

	br label %if45

if44: 
	store i1 1, i1* %cont02

	br label %if45

if45: 

	br label %loop25

	loop27:
	store i1 1, i1* %cont02
	br label %loop50
	loop50:
	%_53 = load i1, i1* %cont02
	br i1 %_53, label %loop51, label %loop52

	loop51:
	%_54 = load i32, i32* %j
	%_55 = sub i32 %_54, 1
	store i32 %_55, i32* %j
	%_56 = getelementptr i8, i8* %this, i32 8
	%_57 = bitcast i8* %_56 to i32**
	%_58 = load i32*, i32** %_57
	%_59 = load i32, i32* %j
	%_60 = load i32, i32* %_58
	%_61 = icmp ult i32 %_59, %_60
	br i1 %_61, label %oob65, label %oob66
	oob65:
	%_62 = add i32 %_59, 1
	%_63 = getelementptr i32, i32* %_58, i32 %_62
	%_64 = load i32, i32* %_63
	br label %oob67
	oob66:
	call void @throw_oob()
	br label %oob67
	oob67:
	store i32 %_64, i32* %aux03
	%_71 = load i32, i32* %v
	%_72 = load i32, i32* %aux03
	%_73 = icmp slt i32 %_71, %_72
	%_74 = xor i1 1, %_73
	br i1 %_74, label %if68, label %if69

if68: 
	store i1 0, i1* %cont02

	br label %if70

if69: 
	store i1 1, i1* %cont02

	br label %if70

if70: 

	br label %loop50

	loop52:
	%_75 = getelementptr i8, i8* %this, i32 8
	%_76 = bitcast i8* %_75 to i32**
	%_77 = load i32*, i32** %_76
	%_78 = load i32, i32* %i
	%_79 = load i32, i32* %_77
	%_80 = icmp ult i32 %_78, %_79
	br i1 %_80, label %oob84, label %oob85
	oob84:
	%_81 = add i32 %_78, 1
	%_82 = getelementptr i32, i32* %_77, i32 %_81
	%_83 = load i32, i32* %_82
	br label %oob86
	oob85:
	call void @throw_oob()
	br label %oob86
	oob86:
	store i32 %_83, i32* %t
	%_88 = getelementptr i8, i8* %this, i32 8
	%_89 = bitcast i8* %_88 to i32**
	%_90 = load i32*, i32** %_89
	%_87 = load i32, i32* %_90
	%_91 = load i32, i32* %i
	%_92 = icmp ult i32 %_91, %_87
	br i1 %_92, label %oob93, label %oob94
	oob93:
	%_96 = add i32 %_91, 1
	%_97 = getelementptr i32, i32* %_90, i32 %_96
	%_98 = getelementptr i8, i8* %this, i32 8
	%_99 = bitcast i8* %_98 to i32**
	%_100 = load i32*, i32** %_99
	%_101 = load i32, i32* %j
	%_102 = load i32, i32* %_100
	%_103 = icmp ult i32 %_101, %_102
	br i1 %_103, label %oob107, label %oob108
	oob107:
	%_104 = add i32 %_101, 1
	%_105 = getelementptr i32, i32* %_100, i32 %_104
	%_106 = load i32, i32* %_105
	br label %oob109
	oob108:
	call void @throw_oob()
	br label %oob109
	oob109:
	store i32 %_106, i32* %_97
	 br label %oob95
	oob94:
	call void @throw_oob()
	 br label %oob95
	oob95:
	%_111 = getelementptr i8, i8* %this, i32 8
	%_112 = bitcast i8* %_111 to i32**
	%_113 = load i32*, i32** %_112
	%_110 = load i32, i32* %_113
	%_114 = load i32, i32* %j
	%_115 = icmp ult i32 %_114, %_110
	br i1 %_115, label %oob116, label %oob117
	oob116:
	%_119 = add i32 %_114, 1
	%_120 = getelementptr i32, i32* %_113, i32 %_119
	%_121 = load i32, i32* %t
	store i32 %_121, i32* %_120
	 br label %oob118
	oob117:
	call void @throw_oob()
	 br label %oob118
	oob118:
	%_125 = load i32, i32* %j
	%_126 = load i32, i32* %i
	%_127 = add i32 %_126, 1
	%_128 = icmp slt i32 %_125, %_127
	br i1 %_128, label %if122, label %if123

if122: 
	store i1 0, i1* %cont01

	br label %if124

if123: 
	store i1 1, i1* %cont01

	br label %if124

if124: 

	br label %loop21

	loop23:
	%_130 = getelementptr i8, i8* %this, i32 8
	%_131 = bitcast i8* %_130 to i32**
	%_132 = load i32*, i32** %_131
	%_129 = load i32, i32* %_132
	%_133 = load i32, i32* %j
	%_134 = icmp ult i32 %_133, %_129
	br i1 %_134, label %oob135, label %oob136
	oob135:
	%_138 = add i32 %_133, 1
	%_139 = getelementptr i32, i32* %_132, i32 %_138
	%_140 = getelementptr i8, i8* %this, i32 8
	%_141 = bitcast i8* %_140 to i32**
	%_142 = load i32*, i32** %_141
	%_143 = load i32, i32* %i
	%_144 = load i32, i32* %_142
	%_145 = icmp ult i32 %_143, %_144
	br i1 %_145, label %oob149, label %oob150
	oob149:
	%_146 = add i32 %_143, 1
	%_147 = getelementptr i32, i32* %_142, i32 %_146
	%_148 = load i32, i32* %_147
	br label %oob151
	oob150:
	call void @throw_oob()
	br label %oob151
	oob151:
	store i32 %_148, i32* %_139
	 br label %oob137
	oob136:
	call void @throw_oob()
	 br label %oob137
	oob137:
	%_153 = getelementptr i8, i8* %this, i32 8
	%_154 = bitcast i8* %_153 to i32**
	%_155 = load i32*, i32** %_154
	%_152 = load i32, i32* %_155
	%_156 = load i32, i32* %i
	%_157 = icmp ult i32 %_156, %_152
	br i1 %_157, label %oob158, label %oob159
	oob158:
	%_161 = add i32 %_156, 1
	%_162 = getelementptr i32, i32* %_155, i32 %_161
	%_163 = getelementptr i8, i8* %this, i32 8
	%_164 = bitcast i8* %_163 to i32**
	%_165 = load i32*, i32** %_164
	%_166 = load i32, i32* %right
	%_167 = load i32, i32* %_165
	%_168 = icmp ult i32 %_166, %_167
	br i1 %_168, label %oob172, label %oob173
	oob172:
	%_169 = add i32 %_166, 1
	%_170 = getelementptr i32, i32* %_165, i32 %_169
	%_171 = load i32, i32* %_170
	br label %oob174
	oob173:
	call void @throw_oob()
	br label %oob174
	oob174:
	store i32 %_171, i32* %_162
	 br label %oob160
	oob159:
	call void @throw_oob()
	 br label %oob160
	oob160:
	%_176 = getelementptr i8, i8* %this, i32 8
	%_177 = bitcast i8* %_176 to i32**
	%_178 = load i32*, i32** %_177
	%_175 = load i32, i32* %_178
	%_179 = load i32, i32* %right
	%_180 = icmp ult i32 %_179, %_175
	br i1 %_180, label %oob181, label %oob182
	oob181:
	%_184 = add i32 %_179, 1
	%_185 = getelementptr i32, i32* %_178, i32 %_184
	%_186 = load i32, i32* %t
	store i32 %_186, i32* %_185
	 br label %oob183
	oob182:
	call void @throw_oob()
	 br label %oob183
	oob183:
	; QS.Sort : 1
	%_187 = bitcast i8* %this to i8***
	%_188 = load i8**, i8*** %_187
	%_189 = getelementptr i8*, i8** %_188, i32 1
	%_190 = load i8*, i8** %_189
	%_191 = bitcast i8* %_190 to i32 (i8*, i32, i32)*
	%_193 = load i32, i32* %left
	%_194 = load i32, i32* %i
	%_195 = sub i32 %_194, 1
	%_192 = call i32 %_191(i8* %this, i32 %_193, i32 %_195)
	store i32 %_192, i32* %nt
	; QS.Sort : 1
	%_196 = bitcast i8* %this to i8***
	%_197 = load i8**, i8*** %_196
	%_198 = getelementptr i8*, i8** %_197, i32 1
	%_199 = load i8*, i8** %_198
	%_200 = bitcast i8* %_199 to i32 (i8*, i32, i32)*
	%_202 = load i32, i32* %i
	%_203 = add i32 %_202, 1
	%_204 = load i32, i32* %right
	%_201 = call i32 %_200(i8* %this, i32 %_203, i32 %_204)
	store i32 %_201, i32* %nt

	br label %if2

if1: 
	store i32 0, i32* %nt

	br label %if2

if2: 
	ret i32 0
}

define i32 @QS.Print(i8* %this) {
	%j = alloca i32
	store i32 0, i32* %j
	br label %loop0
	loop0:
	%_3 = load i32, i32* %j
	%_4 = getelementptr i8, i8* %this, i32 16
	%_5 = bitcast i8* %_4 to i32*
	%_6 = load i32, i32* %_5
	%_7 = icmp slt i32 %_3, %_6
	br i1 %_7, label %loop1, label %loop2

	loop1:
	%_8 = getelementptr i8, i8* %this, i32 8
	%_9 = bitcast i8* %_8 to i32**
	%_10 = load i32*, i32** %_9
	%_11 = load i32, i32* %j
	%_12 = load i32, i32* %_10
	%_13 = icmp ult i32 %_11, %_12
	br i1 %_13, label %oob17, label %oob18
	oob17:
	%_14 = add i32 %_11, 1
	%_15 = getelementptr i32, i32* %_10, i32 %_14
	%_16 = load i32, i32* %_15
	br label %oob19
	oob18:
	call void @throw_oob()
	br label %oob19
	oob19:
	call void (i32) @print_int(i32 %_16)
	%_20 = load i32, i32* %j
	%_21 = add i32 %_20, 1
	store i32 %_21, i32* %j

	br label %loop0

	loop2:
	ret i32 0
}

define i32 @QS.Init(i8* %this, i32 %.sz) {
	%sz = alloca i32
	store i32 %.sz, i32* %sz
	%_0 = load i32, i32* %sz
	%_1 = getelementptr i8, i8* %this, i32 16
	%_2 = bitcast i8* %_1 to i32*
	store i32 %_0, i32* %_2
	%_3 = load i32, i32* %sz
	%_6 = icmp slt i32 %_3, 0
	br i1 %_6, label %oob8, label %oob9
	oob8:
	call void @throw_oob()
	br label %oob9
	oob9:
		%_7 = add i32 %_3, 1
	%_4 = call i8* @calloc(i32 4, i32 %_7)
	%_5 = bitcast i8* %_4 to i32*
	store i32 %_3, i32* %_5
	%_10 = getelementptr i8, i8* %this, i32 8
	%_11 = bitcast i8* %_10 to i32**
	store i32* %_5, i32** %_11
	%_13 = getelementptr i8, i8* %this, i32 8
	%_14 = bitcast i8* %_13 to i32**
	%_15 = load i32*, i32** %_14
	%_12 = load i32, i32* %_15
	%_16 = icmp ult i32 0, %_12
	br i1 %_16, label %oob17, label %oob18
	oob17:
	%_20 = add i32 0, 1
	%_21 = getelementptr i32, i32* %_15, i32 %_20
	store i32 20, i32* %_21
	 br label %oob19
	oob18:
	call void @throw_oob()
	 br label %oob19
	oob19:
	%_23 = getelementptr i8, i8* %this, i32 8
	%_24 = bitcast i8* %_23 to i32**
	%_25 = load i32*, i32** %_24
	%_22 = load i32, i32* %_25
	%_26 = icmp ult i32 1, %_22
	br i1 %_26, label %oob27, label %oob28
	oob27:
	%_30 = add i32 1, 1
	%_31 = getelementptr i32, i32* %_25, i32 %_30
	store i32 7, i32* %_31
	 br label %oob29
	oob28:
	call void @throw_oob()
	 br label %oob29
	oob29:
	%_33 = getelementptr i8, i8* %this, i32 8
	%_34 = bitcast i8* %_33 to i32**
	%_35 = load i32*, i32** %_34
	%_32 = load i32, i32* %_35
	%_36 = icmp ult i32 2, %_32
	br i1 %_36, label %oob37, label %oob38
	oob37:
	%_40 = add i32 2, 1
	%_41 = getelementptr i32, i32* %_35, i32 %_40
	store i32 12, i32* %_41
	 br label %oob39
	oob38:
	call void @throw_oob()
	 br label %oob39
	oob39:
	%_43 = getelementptr i8, i8* %this, i32 8
	%_44 = bitcast i8* %_43 to i32**
	%_45 = load i32*, i32** %_44
	%_42 = load i32, i32* %_45
	%_46 = icmp ult i32 3, %_42
	br i1 %_46, label %oob47, label %oob48
	oob47:
	%_50 = add i32 3, 1
	%_51 = getelementptr i32, i32* %_45, i32 %_50
	store i32 18, i32* %_51
	 br label %oob49
	oob48:
	call void @throw_oob()
	 br label %oob49
	oob49:
	%_53 = getelementptr i8, i8* %this, i32 8
	%_54 = bitcast i8* %_53 to i32**
	%_55 = load i32*, i32** %_54
	%_52 = load i32, i32* %_55
	%_56 = icmp ult i32 4, %_52
	br i1 %_56, label %oob57, label %oob58
	oob57:
	%_60 = add i32 4, 1
	%_61 = getelementptr i32, i32* %_55, i32 %_60
	store i32 2, i32* %_61
	 br label %oob59
	oob58:
	call void @throw_oob()
	 br label %oob59
	oob59:
	%_63 = getelementptr i8, i8* %this, i32 8
	%_64 = bitcast i8* %_63 to i32**
	%_65 = load i32*, i32** %_64
	%_62 = load i32, i32* %_65
	%_66 = icmp ult i32 5, %_62
	br i1 %_66, label %oob67, label %oob68
	oob67:
	%_70 = add i32 5, 1
	%_71 = getelementptr i32, i32* %_65, i32 %_70
	store i32 11, i32* %_71
	 br label %oob69
	oob68:
	call void @throw_oob()
	 br label %oob69
	oob69:
	%_73 = getelementptr i8, i8* %this, i32 8
	%_74 = bitcast i8* %_73 to i32**
	%_75 = load i32*, i32** %_74
	%_72 = load i32, i32* %_75
	%_76 = icmp ult i32 6, %_72
	br i1 %_76, label %oob77, label %oob78
	oob77:
	%_80 = add i32 6, 1
	%_81 = getelementptr i32, i32* %_75, i32 %_80
	store i32 6, i32* %_81
	 br label %oob79
	oob78:
	call void @throw_oob()
	 br label %oob79
	oob79:
	%_83 = getelementptr i8, i8* %this, i32 8
	%_84 = bitcast i8* %_83 to i32**
	%_85 = load i32*, i32** %_84
	%_82 = load i32, i32* %_85
	%_86 = icmp ult i32 7, %_82
	br i1 %_86, label %oob87, label %oob88
	oob87:
	%_90 = add i32 7, 1
	%_91 = getelementptr i32, i32* %_85, i32 %_90
	store i32 9, i32* %_91
	 br label %oob89
	oob88:
	call void @throw_oob()
	 br label %oob89
	oob89:
	%_93 = getelementptr i8, i8* %this, i32 8
	%_94 = bitcast i8* %_93 to i32**
	%_95 = load i32*, i32** %_94
	%_92 = load i32, i32* %_95
	%_96 = icmp ult i32 8, %_92
	br i1 %_96, label %oob97, label %oob98
	oob97:
	%_100 = add i32 8, 1
	%_101 = getelementptr i32, i32* %_95, i32 %_100
	store i32 19, i32* %_101
	 br label %oob99
	oob98:
	call void @throw_oob()
	 br label %oob99
	oob99:
	%_103 = getelementptr i8, i8* %this, i32 8
	%_104 = bitcast i8* %_103 to i32**
	%_105 = load i32*, i32** %_104
	%_102 = load i32, i32* %_105
	%_106 = icmp ult i32 9, %_102
	br i1 %_106, label %oob107, label %oob108
	oob107:
	%_110 = add i32 9, 1
	%_111 = getelementptr i32, i32* %_105, i32 %_110
	store i32 5, i32* %_111
	 br label %oob109
	oob108:
	call void @throw_oob()
	 br label %oob109
	oob109:
	ret i32 0
}

