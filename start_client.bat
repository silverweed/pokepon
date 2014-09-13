@echo off
echo Starting Pokepon Client...
echo Server IP ?
set /p "SERVERIP="
echo Server port?
set /p "SERVERPORT="
echo.
echo Attempting connection with %SERVERIP%:%SERVERPORT% ...
java pokepon.net.jack.client.PokeponClient %SERVERIP% %SERVERPORT%
