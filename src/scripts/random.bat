FOR %%I IN (1,1,5) DO (
FOR %%S IN (rings kaleidoscope) DO (
  CD %%S
  %%S.exe
  timeout /t 30	\
  taskkill /IM javaw.exe
  cd ..
)
)