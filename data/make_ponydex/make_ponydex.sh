#!/bin/bash

if [[ ! -x ./make_ponydex_entry.pl ]]; then
	echo "make_ponydex_entry.pl doesn't exist or is not executable. Aborting."
	exit 1
fi

cat <<EOF
<!DOCTYPE html>
<html>
<head>
  <link rel="stylesheet" href="stylesheet.css" />
  <style>
    .type {
    	border: 1px solid;
	border-radius: 4px;
	width: 100px;
	display: inline-block;
	text-align: center;
	font-weight: bold;
    }
    table.entry th.name {
    	width: 200px;
	text-align: left;
    }
    table.entry img {
    	height: 90px;
	/*width: 90px;*/
    }
    table.entry table.ability {
    	font-style: italic;
    }
    table.entry table.stats {
    	width: 210px;
	margin: 1px;
	border: 1px;
    }
  </style>
</head>

<body>

<table class="ponydex">
EOF

ENTRY_FILES=$(ls pony/*.java | grep -v Pony.java)

for FILE in $ENTRY_FILES; do
	echo "  <tr>"
	echo "    <td>"
	./make_ponydex_entry.pl "$FILE"
	echo "    </td>"
	echo "  </tr>"
done

cat <<EOF
</table>

</body>
</html>
EOF
