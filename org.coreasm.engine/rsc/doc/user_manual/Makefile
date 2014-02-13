mainfile = CoreASM-UserManual

doc:
	pdflatex $(mainfile).tex
	makeindex $(mainfile)
	pdflatex $(mainfile).tex
	pdflatex $(mainfile).tex
	if which okular > /dev/null; then okular $(mainfile).pdf; else open $(mainfile).pdf; fi
clean:
	rm -f *.ps *.pdf *.dvi *.bbl *.aux *.log *.blg
