" Poképon vim syntax highlighting file for team files
"
" by Giacomo Parolini
" Dec 2013

if exists("b:current_syntax")
	finish
endif

" Keywords
syn match pkpMove "-\s\+[A-Za-z\ ()]\+"
"syn match pkpName "\%^[A-Za-z0-9\ ]\+"
"syn match pkpName "^\s*$\n\+[A-Za-z0-9\ ]\+"
syn match pkpName /^[A-Za-z0-9\ ']\+/
syn keyword pkpKeyword Nature EVs IVs Happiness Ability Level
syn match pkpTeamName /^\s*$TEAM_NAME\s\+=\s\+\S.*/
syn match pkpComment "#.*$"

" Contained keywords and stuff
syn match pkpString contained "[A-Za-z0-9\ ]\+" 
syn match pkpEVIV contained "[0-9/]"
syn keyword pkpStat contained hp atk def spatk spdef speed
syn keyword pkpNature contained Friendly Sensitive Diligent Smiley Chaotic Proud Pragmatic Selfish Blackhearted Shy Patient Dependable Taciturn Egghead Solitary Random Bookworm Fabulous Silly Radiant Mysterious Stylish Awesome Cool Radical


" Regions
syn region nickRegion start="\~" end=/\n/ fold transparent contains=pkpString,pkpComment
syn region itemRegion start="@" end=/\n/ fold transparent contains=pkpString,pkpComment
syn region charRegion start=":\s" end=/\n/ fold transparent contains=pkpEVIV,pkpStat,pkpNature,pkpComment

" Identifiers - defining them invalidates the regions definitions.
"syn match pkpIdentifier "\~" nextgroup=pkpName skipwhite
"syn match pkpIdentifier "@" nextgroup=pkpName skipwhite


" Syntax errors
syn match pkpError "\~[^\ ]"
syn match pkpError "[^\ ]\~"
syn match pkpError "\~\s*\n"
syn match pkpError "@[^\ ]"
syn match pkpError "[^\ ]@"
syn match pkpError "@\s*\n"
syn match pkpError "\~\s*@"
syn match pkpError "/[^\ ]"
syn match pkpError "[^\ ]/"

hi link pkpString String
hi link pkpEVIV Number
hi link pkpKeyword Keyword
hi link pkpStat Special
hi link pkpError Error
hi link pkpName Type
hi link pkpMove String
hi link pkpNature Identifier 
hi link pkpIdentifier Identifier
hi link pkpComment Comment
hi link pkpTeamName Identifier 


let b:current_syntax = "pokepon"


