% ----------------------------------------------------------------------------
% FIT thesis
% ----------------------------------------------------------------------------
% Autoři / Authors: 2007 Zdeněk Vašíček, 2008 Michal Bidlo, 2019 Jaroslav Dytrych
% ----------------------------------------------------------------------------
\NeedsTeXFormat{LaTeX2e}
\ProvidesClass{fitthesis}[2022/11/14 Sablona pro BP/DP]

%Vychozi hodnoty parametru / Default values for parameters
\newif\ifczech \let\ifczech\iftrue
\newif\ifslovak \let\ifslovak\iffalse
\newif\ifenslovak \let\ifenslovak\iffalse
\newif\ifenglish \let\ifenglish\iffalse
\newif\ifczechslovak \let\ifczechslovak\iftrue
\newif\ifcover \let\ifcover\iffalse
\newif\ifCOPEN \let\ifCOPEN\iffalse
\newif\ifCBOPEN \let\ifCBOPEN\iffalse
\newif\ifODSAZ \let\ifODSAZ\iffalse

\newif\ifPrint \let\ifPrint\iffalse
\newif\ifBarLogo \let\ifBarLogo\iffalse
\newif\ifWis   \let\ifWis\iftrue
\newif\ifzadani   \let\ifzadani\iffalse
\newif\iftwoside   \let\iftwoside\iffalse
\newif\ifoneside   \let\ifoneside\iftrue
\newif\ifphdsummary   \let\ifphdsummary\iffalse

%-----------------------------------------------------------------------------
% Parametry tridy (odkomentovani zakomentovanych na vlastni nebezpeci)
% Class parameters (uncommenting of commented one is only on your own risk)
%-----------------------------------------------------------------------------
\DeclareOption{english}{\let\ifenglish\iftrue\let\ifczech\iffalse\let\ifczechslovak\iffalse}
\DeclareOption{slovak}{\let\ifslovak\iftrue\let\ifczech\iffalse}
\DeclareOption{enslovak}{\let\ifenslovak\iftrue}
\DeclareOption{cover}{\let\ifcover\iftrue}
\DeclareOption{print}{\let\ifWis\iffalse \let\ifPrint\iftrue}
\DeclareOption{cprint}{\let\ifWis\iffalse \let\ifPrint\iftrue \let\ifBarLogo\iftrue}
\DeclareOption{zadani}{\let\ifzadani\iftrue}
\DeclareOption{twoside}{\let\ifoneside\iffalse \let\iftwoside\iftrue}
\DeclareOption{phdsummary}{\let\ifoneside\iffalse \let\iftwoside\iffalse \let\ifphdsummary\iftrue}
%\DeclareOption{COPEN}{\let\ifCOPEN\iftrue}
%\DeclareOption{CBOPEN}{\let\ifCBOPEN\iftrue}
\DeclareOption{odsaz}{\let\ifODSAZ\iftrue}

\DeclareOption*{}
\ProcessOptions\relax
%Jako vychozi pouzijeme tridu REPORT / We will use REPORT class as a base
\iftwoside
  \ifx\pdfoutput\undefined % nejedeme pod pdftexem / we are not using pdftex
    \LoadClass[a4paper,twoside,onecolumn,11pt]{report}
  \else
    \LoadClass[pdftex,a4paper,twoside,onecolumn,11pt]{report}
  \fi
\fi
\ifoneside
  \ifx\pdfoutput\undefined % nejedeme pod pdftexem
    \LoadClass[a4paper,oneside,onecolumn,11pt]{report}
  \else
    \LoadClass[pdftex,a4paper,oneside,onecolumn,11pt]{report}
  \fi
\fi
\ifphdsummary
  \ifx\pdfoutput\undefined % nejedeme pod pdftexem / we are not using pdftex
    \LoadClass[a5paper,twoside,onecolumn,11pt]{report}
  \else
    \LoadClass[pdftex,a5paper,twoside,onecolumn,11pt]{report}
  \fi
  \twosidetrue
\fi

%-----------------------------------------------------------------------------
% Balicky / Packages
%-----------------------------------------------------------------------------
\ifenglish
  \PassOptionsToPackage{main=english}{babel}
\fi
\usepackage{amsmath}
\usepackage{amssymb}
\usepackage[czech,slovak,english]{babel}
\usepackage[utf8]{inputenc} %kodovani / encoding
\usepackage{cmap}
\usepackage[T1]{fontenc}
\usepackage{url}
\usepackage{ragged2e}
\usepackage{color}
\usepackage{etoolbox}
\usepackage{enumitem}

\ifczech
  \main@language{czech}
\else
  \ifslovak
    \main@language{slovak}
  \else
    \main@language{english}
  \fi
\fi
\ifx\pdfoutput\undefined  % nejedeme pod pdftexem / we are not using pdftex
  \usepackage{graphics}
  \usepackage{epsfig}
\else % je to pdftex ! / it is pdftex !
  \ifzadani
    \usepackage{pdfpages}
  \else % neni potreba pdfpages / pdfpages is not necessary
    \usepackage[pdftex]{graphicx}
  \fi
\fi

\newtoggle{phd_summary}
\togglefalse{phd_summary}
\ifphdsummary
  \toggletrue{phd_summary}
\fi

\newtoggle{slovak_used}
\togglefalse{slovak_used}

\ifslovak
  \toggletrue{slovak_used}
\fi

\newtoggle{english_used}
\togglefalse{english_used}

\ifenglish
  \toggletrue{english_used}
\fi

\newtoggle{enslovak_used}
\togglefalse{enslovak_used}

\ifenslovak
  \toggletrue{enslovak_used}
\fi

\newtoggle{czech_used}
\togglefalse{czech_used}

\ifczech
  \toggletrue{czech_used}
\fi

\newtoggle{czechslovak}
\togglefalse{czechslovak}
\ifczech
  \toggletrue{czechslovak}
\fi
\ifslovak
  \toggletrue{czechslovak}
\fi

\newtoggle{zadaniTwop}
\togglefalse{zadaniTwop}
\iftwoside
  \toggletrue{zadaniTwop}
\fi
\iftoggle{zadaniTwop}{
  \ifzadani
    \toggletrue{zadaniTwop}
  \else
    \togglefalse{zadaniTwop}
  \fi
}{}


%-----------------------------------------------------------------------------
% Sazebni obrazec / Page layout
%-----------------------------------------------------------------------------
\iftwoside
%  \usepackage[left=3.46cm, text={15.2cm,23.47cm}, top=2.5cm, marginparwidth=50pt]{geometry}
  \usepackage[a4paper]{geometry}
  \newgeometry{left=3.46cm, text={15.2cm,23.47cm}, top=2.5cm, marginparwidth=50pt}
\else
  \setlength{\hoffset}{-0.7cm}
  \setlength{\voffset}{-1.5cm}
  \setlength{\textheight}{23.0cm}
  \setlength{\textwidth}{15.2cm}
\fi

\ifphdsummary
  \newgeometry{left=1.8cm, text={9.5cm,16.5cm}, top=2.0cm, marginparwidth=25pt}
\fi

\ifODSAZ
  \setlength{\parindent}{0pt}
  \setlength{\parskip}{0.5\bigskipamount}
\else
  \setlength\parindent{17\p@}
  \setlength{\parskip}{0pt}
\fi


%-----------------------------------------------------------------------------
% Texty / Texts
%-----------------------------------------------------------------------------
\def\@vut@CS{Vysoké učení technické v~Brně}
\def\@vut@SK{Vysoké učení technické v~Brně}
\def\@vut@EN{Brno University of Technology}
\def\@author@CS{Autor práce}
\def\@author@SK{Autor práce}
\def\@author@EN{Author}
\def\@abstract@CS{Abstrakt}
\def\@abstract@SK{Abstrakt}
\def\@abstract@EN{Abstract}
\def\@extendedabstract@CS{Rozšířený abstrakt}
\def\@extendedabstract@SK{Rozšírený abstrakt}
\def\@keywords@CS{Klíčová slova}
\def\@keywords@SK{Kľúčové slová}
\def\@keywords@EN{Keywords}
\def\@cite@CS{Citace}
\def\@cite@SK{Citácia}
\def\@cite@EN{Reference}
\def\@tdecl@CS{Prohlášení}
\def\@tdecl@SK{Prehlásenie}
\def\@tdecl@EN{Declaration}
\def\@tackn@CS{Poděkování}
\def\@tackn@SK{Poďakovanie}
\def\@tackn@EN{Acknowledgements}

\newif\ifnasel
\def\ifEqString#1#2#3#4{\def\testa{#1}\def\testb{#2}%
  \ifx\testa\testb \def#4{#3} \naseltrue \fi%
}

%Vyber typu projektu / project type selection
\newcommand{\set@project}[1]{%
 \naselfalse
 \ifEqString{#1}{BP}{Bakalářská práce}{\@projecttype@CS}%
% \ifEqString{#1}{BP}{Bakalárska práca}{\@projecttype@SK}%
 \ifEqString{#1}{BP}{Bakalářská práce}{\@projecttype@SK}%
 \ifEqString{#1}{BP}{Bachelor's thesis}{\@projecttype@EN}%
 \ifEqString{#1}{BP}{Vedoucí práce}{\@supervisor@CS}%
% \ifEqString{#1}{BP}{Vedúci práce}{\@supervisor@SK}%
 \ifEqString{#1}{BP}{Vedoucí práce}{\@supervisor@SK}%
 \ifEqString{#1}{BP}{Supervisor}{\@supervisor@EN}%
 \ifEqString{#1}{SP}{Semestrální projekt}{\@projecttype@CS}%
% \ifEqString{#1}{SP}{Semestrálny projekt}{\@projecttype@SK}%
 \ifEqString{#1}{SP}{Semestrální projekt}{\@projecttype@SK}%
 \ifEqString{#1}{SP}{Term project}{\@projecttype@EN}%
 \ifEqString{#1}{SP}{Vedoucí práce}{\@supervisor@CS}%
% \ifEqString{#1}{SP}{Vedúci práce}{\@supervisor@SK}%
 \ifEqString{#1}{SP}{Vedoucí práce}{\@supervisor@SK}%
 \ifEqString{#1}{SP}{Supervisor}{\@supervisor@EN}%
 \ifEqString{#1}{DP}{Diplomová práce}{\@projecttype@CS}%
% \ifEqString{#1}{DP}{Diplomová práca}{\@projecttype@SK}%
 \ifEqString{#1}{DP}{Diplomová práce}{\@projecttype@SK}%
 \ifEqString{#1}{DP}{Master's thesis}{\@projecttype@EN}%
 \ifEqString{#1}{DP}{Vedoucí práce}{\@supervisor@CS}%
% \ifEqString{#1}{DP}{Vedúci práce}{\@supervisor@SK}%
 \ifEqString{#1}{DP}{Vedoucí práce}{\@supervisor@SK}%
 \ifEqString{#1}{DP}{Supervisor}{\@supervisor@EN}%
 \ifEqString{#1}{DR}{Disertační práce}{\@projecttype@CS}%
% \ifEqString{#1}{DR}{Dizertačná práca}{\@projecttype@SK}%
 \ifEqString{#1}{DR}{Disertační práce}{\@projecttype@SK}%
 \ifEqString{#1}{DR}{PhD thesis}{\@projecttype@EN}%
 \ifEqString{#1}{DR}{Školitel}{\@supervisor@CS}%
% \ifEqString{#1}{DR}{Školiteľ}{\@supervisor@SK}%
 \ifEqString{#1}{DR}{Školitel}{\@supervisor@SK}%
 \ifEqString{#1}{DR}{Supervisor}{\@supervisor@EN}%
 \ifEqString{#1}{DA}{Teze disertační práce -- autoreferát}{\@projecttype@CS}%
% \ifEqString{#1}{DA}{Tézy dizertačnej práce -- autoreferát}{\@projecttype@SK}%
 \ifEqString{#1}{DA}{Teze disertační práce -- autoreferát}{\@projecttype@SK}%
 \ifEqString{#1}{DA}{PhD thesis summary}{\@projecttype@EN}%
 \ifEqString{#1}{DA}{Školitel}{\@supervisor@CS}%
% \ifEqString{#1}{DA}{Školiteľ}{\@supervisor@SK}%
 \ifEqString{#1}{DA}{Školitel}{\@supervisor@SK}%
 \ifEqString{#1}{DA}{Supervisor}{\@supervisor@EN}%
 \ifEqString{#1}{DT}{Pojednání k tématu disertační práce}{\@projecttype@CS}%
% \ifEqString{#1}{DT}{Teze disertační práce}{\@projecttype@CS}%
% \ifEqString{#1}{DT}{Pojednanie k téme dizertačnej práce}{\@projecttype@SK}%
 \ifEqString{#1}{DT}{Pojednání k tématu disertační práce}{\@projecttype@SK}%
 \ifEqString{#1}{DT}{PhD thesis proposal}{\@projecttype@EN}%
 \ifEqString{#1}{DT}{Školitel}{\@supervisor@CS}%
% \ifEqString{#1}{DT}{Školiteľ}{\@supervisor@SK}%
 \ifEqString{#1}{DT}{Školitel}{\@supervisor@SK}%
 \ifEqString{#1}{DT}{Supervisor}{\@supervisor@EN}%
 \ifEqString{#1}{PP}{Projektová praxe}{\@projecttype@CS}%
 \ifEqString{#1}{PP}{Projektová prax}{\@projecttype@SK}%
 \ifEqString{#1}{PP}{Project practice}{\@projecttype@EN}%
 \ifEqString{#1}{PP}{Vedoucí}{\@supervisor@CS}%
 \ifEqString{#1}{PP}{Vedúcí}{\@supervisor@SK}%
 \ifEqString{#1}{PP}{Supervisor}{\@supervisor@EN}%
 \ifnasel
 \else
   \typeout{Spatny typ projektu! Moznosti: BP, SP, DP, DR, DT, DA, PP}\set@project{BP}
 \fi
}

%Vyber fakulty / faculty selection
\newcommand{\set@faculty}[3]{%
 \naselfalse
 \ifEqString{#1}{FIT}{Fakulta informačních technologií}{\@fit@CS}%
 \ifEqString{#1}{FIT}{Fakulta informačních technologií}{\@fit@SK}%
 \ifEqString{#1}{FIT}{Faculty of Information Technology}{\@fit@EN}%
 \ifEqString{#1}{FEKT}{Fakulta elektrotechniky 
a komunikačních technologií}{\@fit@CS}%
 \ifEqString{#1}{FEKT}{Fakulta elektrotechniky 
a komunikačních technologií}{\@fit@SK}%
 \ifEqString{#1}{FEKT}{Faculty of Electrical Engineering 
and Communication}{\@fit@EN}%
 \ifEqString{#1}{FSI}{Fakulta strojního inženýrství}{\@fit@CS}%
 \ifEqString{#1}{FSI}{Fakulta strojního inženýrství}{\@fit@SK}%
 \ifEqString{#1}{FSI}{Faculty of Mechanical 
Engineering}{\@fit@EN}%
 \ifEqString{#1}{FA}{Fakulta architektury}{\@fit@CS}%
 \ifEqString{#1}{FA}{Fakulta architektury}{\@fit@SK}%
 \ifEqString{#1}{FA}{Faculty of Architecture}{\@fit@EN}%
 \ifEqString{#1}{FCH}{Fakulta chemická}{\@fit@CS}%
 \ifEqString{#1}{FCH}{Fakulta chemická}{\@fit@SK}%
 \ifEqString{#1}{FCH}{Faculty of Chemistry}{\@fit@EN}%
 \ifEqString{#1}{FP}{Fakulta podnikatelská}{\@fit@CS}%
 \ifEqString{#1}{FP}{Fakulta podnikatelská}{\@fit@SK}%
 \ifEqString{#1}{FP}{Faculty of Business and 
Management}{\@fit@EN}%
 \ifEqString{#1}{FAST}{Fakulta stavební}{\@fit@CS}%
 \ifEqString{#1}{FAST}{Fakulta stavební}{\@fit@SK}%
 \ifEqString{#1}{FAST}{Faculty of Civil Engineering}{\@fit@EN}%
 \ifEqString{#1}{FAVU}{Fakulta výtvarných umění}{\@fit@CS}%
 \ifEqString{#1}{FAVU}{Fakulta výtvarných umění}{\@fit@SK}%
 \ifEqString{#1}{FAVU}{Faculty of Fine Arts}{\@fit@EN}%
 \ifEqString{#1}{USI}{Ústav soudního inženýrství}{\@fit@CS}%
 \ifEqString{#1}{USI}{Ústav soudního inženýrství}{\@fit@SK}%
 \ifEqString{#1}{USI}{Institute of Forensic Engineering}{\@fit@EN}%
 \ifEqString{#1}{DEF}{#2}{\@fit@CS}%
 \ifEqString{#1}{DEF}{#2}{\@fit@SK}%
 \ifEqString{#1}{DEF}{#3}{\@fit@EN}%
 \ifnasel
 \else
   \typeout{Spatna fakulta! Moznosti: FIT, FEKT, FSI, FA, FCH, FP, FAST, FAVU, USI}\set@faculty{FIT}
 \fi
}

%Vyber ustavu / department selection
\newcommand{\set@department}[3]{%
 \naselfalse
 \ifEqString{#1}{UIFS}{Ústav informačních systémů}{\@department@CS}%
 \ifEqString{#1}{UIFS}{Ústav informačních systémů}{\@department@SK}%
 \ifEqString{#1}{UIFS}{Department of Information Systems}{\@department@EN}%
 \ifEqString{#1}{UPSY}{Ústav počítačových systémů}{\@department@CS}%
 \ifEqString{#1}{UPSY}{Ústav počítačových systémů}{\@department@SK}%
 \ifEqString{#1}{UPSY}{Department of Computer Systems}{\@department@EN}%
 \ifEqString{#1}{UITS}{Ústav inteligentních systémů}{\@department@CS}%
 \ifEqString{#1}{UITS}{Ústav inteligentních systémů}{\@department@SK}%
 \ifEqString{#1}{UITS}{Department of Intelligent Systems}{\@department@EN}%
 \ifEqString{#1}{UPGM}{Ústav počítačové grafiky a multimédií}{\@department@CS}%
 \ifEqString{#1}{UPGM}{Ústav počítačové grafiky a multimédií}{\@department@SK}%
 \ifEqString{#1}{UPGM}{Department of Computer Graphics and~Multimedia}{\@department@EN}%
 \ifEqString{#1}{DEF}{#2}{\@department@CS}%
 \ifEqString{#1}{DEF}{#2}{\@department@SK}%
 \ifEqString{#1}{DEF}{#3}{\@department@EN}%
 \ifnasel
 \else
   \typeout{Spatny ustav! Moznosti: UPSY, UIFS, UITS, UPGM, DEF}\set@department{UIFS}
 \fi
}

%Nazev ustavu (CS, SK, EN) / name of the department
\def\@departmentname@CS{\@department@CS}
\def\@departmentname@SK{\@department@SK}
\def\@departmentname@EN{\@department@EN}

%Nazev prace (v jazyce prace) / thesis title (in the language of the thesis)
\iftoggle{slovak_used}{
  \def\@title{\@title@CS}
}{
  \def\@title{\ifczech\@title@CS\else\@title@EN\fi}
}

%Jmeno, prijmeni, tituly autora / name surname and the titles of the author
\def\get@author{\ifx\undefined\@author@tp\else%
\@author@tp~\fi\MakeUppercase{\@author}\ifx\undefined\@author@ta\else,~\@author@ta\fi}
\def\getlc@author{\ifx\undefined\@author@tp\else%
\@author@tp~\fi\@author\ifx\undefined\@author@ta\else,~\@author@ta\fi}
%Jmeno, prijmeni, tituly vedouciho / name surname and the titles of the supervisor
\def\get@supervisor{\ifx\undefined\@supervisor@tp\else%
\@supervisor@tp~\fi\MakeUppercase{\@supervisor}\ifx\undefined\@supervisor@ta\else,~\@supervisor@ta\fi}

%-----------------------------------------------------------------------------
% Projectinfo - parametrizace sablony / template parameters
%-----------------------------------------------------------------------------
%Parametry 
\define@key{projinfo}{title.cs}{\global\def\@title@CS{#1}}
\define@key{projinfo}{title.en}{\global\def\@title@EN{#1}}
\define@key{projinfo}{faculty.cs}{\def\@faculty@CS{#1}}
\define@key{projinfo}{faculty.en}{\def\@faculty@EN{#1}}
\define@key{projinfo}{faculty}{\set@faculty{#1}{\@faculty@CS}{\@faculty@EN}}
\define@key{projinfo}{department.cs}{\def\@departmentn@CS{#1}}
\define@key{projinfo}{department.en}{\def\@departmentn@EN{#1}}
\define@key{projinfo}{department}{\set@department{#1}{\@departmentn@CS}{\@departmentn@EN}}
\define@key{projinfo}{project}{\set@project{#1}}
\define@key{projinfo}{year}{\def\@year{#1}}
\define@key{projinfo}{date}{\def\@date{#1}}
%\define@key{projinfo}{author}{\def\@author{#1}}
\define@key{projinfo}{author.name}{\def\@author@n{#1}}
\define@key{projinfo}{author.surname}{\def\@author@s{#1}}
\define@key{projinfo}{author.title.a}{\def\@author@ta{#1}}
\define@key{projinfo}{author.title.p}{\def\@author@tp{#1}}
\define@key{projinfo}{supervisor.name}{\def\@supervisor@n{#1}}
\define@key{projinfo}{supervisor.surname}{\def\@supervisor@s{#1}}
\define@key{projinfo}{supervisor.title.a}{\def\@supervisor@ta{#1}}
\define@key{projinfo}{supervisor.title.p}{\def\@supervisor@tp{#1}}
\define@key{projinfo}{keywords.cs}{\def\@tkeywords@CS{#1}}
\define@key{projinfo}{keywords.en}{\def\@tkeywords@EN{#1}}
\define@key{projinfo}{abstract.cs}{\def\@tabstract@CS{#1}}
\define@key{projinfo}{abstract.en}{\def\@tabstract@EN{#1}}
\define@key{projinfo}{extendedabstract}{\def\@textendedabstract{#1}}
\define@key{projinfo}{extabstract.odd}{\def\@extabstracto{#1}}
\define@key{projinfo}{declaration}{\def\@declaration{#1}}
\define@key{projinfo}{acknowledgment}{\def\@acknowledgment{#1}}
\define@key{projinfo}{title.length}{\def\@titlelen{#1}}
\define@key{projinfo}{sectitle.length}{\def\@sectitlelen{#1}}
\define@key{projinfo}{dectitle.length}{\def\@dectitlelen{#1}}

%Vychozi hodnoty / Default values
\setkeys{projinfo}{
  title.cs={Název práce (česky)},title.en={Název práce(anglicky)}, 
  date=\today, project=BP, department=DEF, declaration=, 
  abstract.cs=, abstract.en=, keywords.cs=, keywords.en=,
  author.name=Jméno, author.surname=Příjmení, supervisor.name=Jméno, 
  supervisor.surname=Příjmení, extendedabstract=, extabstract.odd=,
  department.cs=,department.en=,title.length=,
  sectitle.length=,dectitle.length=,
  faculty.cs=,faculty.en=,faculty=FIT
}%

\def\projectinfo{\setkeys{projinfo}}

\def\@author{\@author@n\ \@author@s}
\def\@supervisor{\@supervisor@n\ \@supervisor@s}

%Prohlaseni / Declaration
\newcommand{\declaration}[1]{\def\@declaration{#1}}

%Podekovani / Acknowledgement
\newcommand{\acknowledgment}[1]{\def\@acknowledgment{#1}}

%Abstrakt / Abstract
\renewcommand{\abstract}[2][cs]{%
 \ifEqString{#1}{cs}{#2}{\@tabstract@CS}%
 \ifEqString{#1}{sk}{#2}{\@tabstract@SK}%
 \ifEqString{#1}{en}{#2}{\@tabstract@EN}%
}

%Klicova slova / Keywords
\newcommand{\keywords}[2][cs]{%
 \ifEqString{#1}{cs}{#2}{\@tkeywords@CS}%
 \ifEqString{#1}{sk}{#2}{\@tkeywords@SK}%
 \ifEqString{#1}{en}{#2}{\@tkeywords@EN}%
}

%Rozsireny abstrakt / Extended abstract
\newcommand{\extendedabstract}[1]{%
 \def\@textendedabstract{#1}%
}

% Začít rozšířený abstrakt na liché stránce? / Should extended abstract start on the odd page?
\newcommand{\extabstractodd}[1]{%
 \def\@extabstracto{#1}%
}

%Délka titulku / Title length
\newcommand{\titlelength}[1]{%
 \def\@titlelen{#1}%
}

%Délka druhého titulku / Second title length
\newcommand{\sectitlelength}[1]{%
 \def\@sectitlelen{#1}%
}

%Délka titulku nad prohlášením / Length of the title above declaration
\newcommand{\dectitlelength}[1]{%
 \def\@dectitlelen{#1}%
}

\newlength{\mkspc}
\setlength{\mkspc}{2pt}

\def\setfontsize#1{\renewcommand{\baselinestretch}{1.5}\fontsize{#1}{\f@baselineskip}\selectfont}
%text
\newcommand{\mktxt}[2]{\vbox{\setfontsize{#1}\noindent#2}\vspace\mkspc}
%text pro titulní list / text for title page
\newcommand{\mktxtt}[2]{\vbox{\setfontsize{#1}\fosfamily\bfseries\noindent#2}\vspace\mkspc}
%text pro desky / text for cover
\newcommand{\mktxtc}[2]{\vbox{\setfontsize{#1}\ifCOPEN\fosfamily\fi\ifCBOPEN\fosfamily\bfseries\fi\noindent#2}\vspace\mkspc}
%všechna velká / uppercase
\newcommand{\mkuctxt}[2]{\vbox{\setfontsize{#1}\noindent\MakeUppercase{#2}}\vspace\mkspc}
\newcommand{\mkuctxtt}[2]{\vbox{\setfontsize{#1}\fosfamily\bfseries\noindent\MakeUppercase{#2}}\vspace\mkspc}
% Pro název práce na 2 řádky / For the 2-line thesis title
\newcommand{\mkuctxttl}[3]{\vbox to 1.2cm{\noindent\parbox{#2}{\setfontsize{#1}\fosfamily\bfseries\noindent\MakeUppercase{#3}}}\vspace\mkspc}
% Pro 2. název práce na 2 řádky / For the 2-line second thesis title
\newcommand{\mkuctxttls}[3]{\vbox to 1.0cm{\noindent\parbox{#2}{\setfontsize{#1}\fosfamily\bfseries\noindent\baselineskip=13pt\MakeUppercase{#3}}}\vspace\mkspc}
\newcommand{\mkuctxtc}[2]{\vbox{\setfontsize{#1}\ifCOPEN\fosfamily\fi\ifCBOPEN\fosfamily\bfseries\fi\noindent\MakeUppercase{#2}}\vspace\mkspc}
%bold
\newcommand{\mktxt@b}[2]{\vbox{\setfontsize{#1}\noindent\bf#2}\vspace\mkspc}
%bold na 2 řádky / 2-line bold
\newcommand{\mktxtl@b}[3]{\vbox to 1.0cm{\noindent\parbox{#2}{\setfontsize{#1}\noindent\bf\baselineskip=18pt #3}}\vspace\mkspc}
%bold pro titulní list / bold for title page
\newcommand{\mktxtt@b}[2]{\vbox{\setfontsize{#1}\fosfamily\bfseries\noindent\bf#2}\vspace\mkspc}

%-----------------------------------------------------------------------------
% Desky / Cover \makecover
%-----------------------------------------------------------------------------
\newcommand\makecover{
\thispagestyle{empty}\null%

\setlength\parindent{17\p@}
\setlength{\parskip}{0pt}

\fontfamily{qhv}\selectfont%
%  

\addtolength{\voffset}{-0.95cm}

\begin{center}

\ifczech
    \noindent\hspace{-34.0pt}\mkuctxtc{20}{\@vut@CS}
\else
  \ifslovak
    \noindent\hspace{-34.0pt}\mkuctxtc{20}{\@vut@CS}
  \else
    \noindent\hspace{-34.0pt}\mkuctxtc{20}{\@vut@EN}
  \fi
\fi

\vspace{8mm}

\ifczech
  \noindent\hspace{-34.0pt}\mktxtc{20}{\@fit@CS}
\else
  \ifslovak
    \noindent\hspace{-34.0pt}\mktxtc{20}{\@fit@CS}
  \else
    \noindent\hspace{-34.0pt}\mktxtc{20}{\@fit@EN}
  \fi
\fi

\vspace{9cm}

\ifczech
  \noindent\hspace{-34.0pt}\mkuctxtc{20}{\@projecttype@CS}
\else
  \ifslovak
    \noindent\hspace{-34.0pt}\mkuctxtc{20}{\@projecttype@CS}
  \else
    \noindent\hspace{-34.0pt}\mkuctxtc{20}{\@projecttype@EN}
  \fi
\fi

\enlargethispage{5.1\baselineskip}

\vfill

\noindent\mktxtc{16}{\hspace{-52.0pt}Brno, \@year \hfill \getlc@author \hspace{5.0pt}~}

\ifODSAZ
  \setlength{\parindent}{0pt}
  \setlength{\parskip}{0.5\bigskipamount}
\else
  \setlength\parindent{17\p@}
  \setlength{\parskip}{0pt}
\fi

\end{center}

%===================================================
\newpage
\fontfamily{\familydefault}\selectfont%
\addtolength{\voffset}{0.95cm}
}

%-----------------------------------------------------------------------------
% Abstrakt, klicova slova \makeabstract
% Abstract, keywords \makeabstract
%-----------------------------------------------------------------------------
\newcommand\makeabstract{%
\thispagestyle{empty}\null%

\ifczech
\mktxt@b{16}{\@abstract@CS}\noindent\@tabstract@CS
\else
  \ifslovak
    \mktxt@b{16}{\@abstract@SK}\noindent\@tabstract@CS
  \else
    \mktxt@b{16}{\@abstract@EN}\noindent\@tabstract@EN
  \fi
\fi

\vfill

\iftoggle{czech_used}{
\mktxt@b{16}{\@abstract@EN}\noindent\@tabstract@EN
}{
  \ifslovak
    \mktxt@b{16}{\@abstract@EN}\noindent\@tabstract@EN
  \else
    \ifenslovak
      \mktxt@b{16}{\@abstract@SK}\noindent\@tabstract@CS
    \else
      \mktxt@b{16}{\@abstract@CS}\noindent\@tabstract@CS
    \fi
  \fi
}

\vfill

\ifczech
\mktxt@b{16}{\@keywords@CS}\noindent\@tkeywords@CS
\else
  \ifslovak
    \mktxt@b{16}{\@keywords@SK}\noindent\@tkeywords@CS
  \else
    \mktxt@b{16}{\@keywords@EN}\noindent\@tkeywords@EN
  \fi
\fi

\vfill

\iftoggle{czech_used}{
\mktxt@b{16}{\@keywords@EN}\noindent\@tkeywords@EN
}{
  \ifslovak
    \mktxt@b{16}{\@keywords@EN}\noindent\@tkeywords@EN
  \else
    \ifenslovak
      \mktxt@b{16}{\@keywords@SK}\noindent\@tkeywords@CS
    \else
      \mktxt@b{16}{\@keywords@CS}\noindent\@tkeywords@CS
    \fi
  \fi
}

\vfill

\mktxt@b{16}{\ifslovak\@cite@SK\else\ifczech\@cite@CS\else\@cite@EN\fi\fi}
\iftoggle{slovak_used}{
  \noindent\MakeUppercase{\@author@s}, \@author@n. \it\@title\rm. Brno, \@year. \@projecttype@SK. \@vut@SK, \@fit@SK. {\@supervisor@SK} {\ifx\undefined\@supervisor@tp\else\@supervisor@tp ~\fi}{\@supervisor@n} {\@supervisor@s}{\ifx\undefined\@supervisor@ta\else,~\@supervisor@ta\fi}
}{
  \noindent\MakeUppercase{\@author@s}, \@author@n. \it\@title\rm. Brno, \@year. \ifczech\@projecttype@CS\else\@projecttype@EN\fi. \ifczech\@vut@CS\else\@vut@EN\fi, \ifczech\@fit@CS\else\@fit@EN\fi. {\ifczech\@supervisor@CS\else\@supervisor@EN\fi} {\ifx\undefined\@supervisor@tp\else\@supervisor@tp ~\fi}{\@supervisor@n} {\@supervisor@s}{\ifx\undefined\@supervisor@ta\else,~\@supervisor@ta\fi}
}
%===================================================
\newpage

}

%Rozsireny abstrakt / Extended abstract
\newcommand\makeextendedabstract{%
  %Pro rozšířený abstrakt začínající na liché stránce / For extended abstract starting on the odd page
  \newtoggle{extabstract_odd}
  \togglefalse{extabstract_odd}
  
  \ifx\@extabstracto\@empty
  \else
    \toggletrue{extabstract_odd}
  \fi

  \pagestyle{empty}%
  \iftwoside
    \iftoggle{extabstract_odd}{
      \cleardoublepage
    }{}
  \fi
  \ifenslovak
    \mktxt@b{16}{\@extendedabstract@SK}\noindent\@textendedabstract
  \else
    \mktxt@b{16}{\@extendedabstract@CS}\noindent\@textendedabstract
  \fi
  \iftwoside
    \iftoggle{extabstract_odd}{
      \cleardoublepage
    }{
      \newpage
    }
  \else
    \newpage
  \fi
  \pagestyle{plain}
}

%-----------------------------------------------------------------------------
% Prohlaseni, podekovani \makedeclaration
% Declaration, acknowledgement \makedeclaration
%-----------------------------------------------------------------------------
\newcommand\makedeclaration{
\pagestyle{empty}%

  
\iftoggle{long_dectitle}{
  \mktxtl@b{18}{\@dectitlelen}{\@title}
}{
  \mktxt@b{18}{\@title}
}

\ifx\@declaration\@empty
\else
  \vspace{10mm}
  \ifslovak
    \mktxt@b{16}{\@tdecl@SK}
  \else
    \mktxt@b{16}{\ifczech\@tdecl@CS\else\@tdecl@EN\fi}
  \fi

  \noindent\@declaration

  \begin{flushright}
      \makebox[4cm]{\dotfill} \\
      \@author\\
      \@date
  \end{flushright}
\fi 

\ifx\undefined\@acknowledgment
\else
\vspace{20mm}
\ifslovak
  \mktxt@b{16}{\@tackn@SK}
\else
  \mktxt@b{16}{\ifczech\@tackn@CS\else\@tackn@EN\fi}
\fi
\noindent\@acknowledgment
\fi

\vfill

%===================================================
\iftwoside
  \cleardoublepage
\else
  \newpage
\fi
\pagestyle{plain}
}

%-----------------------------------------------------------------------------
% Uvodni stranky \maketitle
% Introductory pages \maketitle
%-----------------------------------------------------------------------------
\renewcommand\maketitle{%
\ifcover\makecover\fi%
\thispagestyle{empty}\null%
\iftwoside
  \pagestyle{empty}\null%
\fi

\setlength\parindent{17\p@}
\setlength{\parskip}{0pt}

%=====================================================
\fontfamily{qhv}\selectfont%
%\newsavebox{\@obrlogo}\newsavebox{\@rtext}\newlength{\@vyskartext}\newlength{\@vyskaobr}%
\newsavebox{\@obrlogo}\newsavebox{\@rtext}\newlength{\@vyskartext}\newlength{\@sirkartext}\newlength{\@vyskaobr}\newlength{\@sirkaobr}%

\iftoggle{phd_summary}{

\ifPrint
  \ifBarLogo
    \savebox{\@obrlogo}{\hspace*{-0.6cm}\includegraphics[width=2.5cm,keepaspectratio,trim={1.2cm 1.2cm 1.2cm 1.2cm},clip]{template-fig/VUT_symbol_barevne_CMYK_CZ}}%symbol VUT
  \else
    \savebox{\@obrlogo}{\hspace*{-0.6cm}\includegraphics[width=2.5cm,keepaspectratio,trim={1.2cm 1.2cm 1.2cm 1.2cm},clip]{template-fig/VUT_symbol_cernobile_CZ}}%symbol VUT
  \fi
\else
  \savebox{\@obrlogo}{\hspace*{-0.6cm}\includegraphics[width=2.5cm,keepaspectratio,trim={1.2cm 1.2cm 1.2cm 1.2cm},clip]{template-fig/VUT_symbol_barevne_CMYK_CZ}}%symbol VUT
\fi

}{

\ifPrint
  \ifBarLogo
    \savebox{\@obrlogo}{\hspace*{-0.6cm}\includegraphics[width=4.1cm,keepaspectratio,trim={1.2cm 1.2cm 1.2cm 1.2cm},clip]{template-fig/VUT_symbol_barevne_CMYK_CZ}}%symbol VUT
  \else
    \savebox{\@obrlogo}{\hspace*{-0.6cm}\includegraphics[width=4.1cm,keepaspectratio,trim={1.2cm 1.2cm 1.2cm 1.2cm},clip]{template-fig/VUT_symbol_cernobile_CZ}}%symbol VUT
  \fi
\else
  \savebox{\@obrlogo}{\hspace*{-0.6cm}\includegraphics[width=4.1cm,keepaspectratio,trim={1.2cm 1.2cm 1.2cm 1.2cm},clip]{template-fig/VUT_symbol_barevne_CMYK_CZ}}%symbol VUT
\fi

}

\iftoggle{phd_summary}{
  \savebox{\@rtext}{\vbox{\mkuctxt{16}{\ifslovak\@vut@SK\else\ifczech\@vut@CS\else\@vut@EN\fi\fi}\mkuctxt{9}{\ifczech\@vut@EN\else\ifslovak\@vut@EN\else\@vut@CS\fi\fi}}}%text VUT
}{
  \savebox{\@rtext}{\vbox{\mkuctxt{18}{\ifslovak\@vut@SK\else\ifczech\@vut@CS\else\@vut@EN\fi\fi}\mkuctxt{11}{\ifczech\@vut@EN\else\ifslovak\@vut@EN\else\@vut@CS\fi\fi}}}%text VUT
}

\settoheight{\@vyskaobr}{\makebox{\usebox{\@obrlogo}}}% vyska loga / logo height
\settowidth{\@sirkaobr}{\makebox{\usebox{\@obrlogo}}}% vyska loga
\settoheight{\@vyskartext}{\makebox{\usebox{\@rtext}}}% vyska textu / text height
\settowidth{\@sirkartext}{\makebox{\usebox{\@rtext}}}% sirka textu / text width
\addtolength{\@vyskaobr}{-\@vyskartext}%odecteme od vysky loga vysku textu / we will subtract text height from logo height
\addtolength{\@sirkartext}{-\@sirkaobr}\addtolength{\@sirkartext}{-10pt}%sirka textu na radku s obrazkem a odsazenim / text width on row with picture and indentation

\newtoggle{gray_used}
\togglefalse{gray_used}


\usebox{\@obrlogo}

\iftoggle{phd_summary}{

  \vspace{5mm}
  
  \iftoggle{slovak_used}{
    \mkuctxtt{16}{\@vut@SK}
    \mkuctxtt{9}{\@vut@EN}
  }{
    \iftoggle{enslovak_used}{
      \mkuctxtt{16}{\@vut@EN}
      \mkuctxtt{9}{\@vut@SK}
    }{
      \mkuctxtt{16}{\ifczech\@vut@CS\else\@vut@EN\fi}
      \mkuctxtt{9}{\ifczech\@vut@EN\else\@vut@CS\fi}
    }
  }

  \vspace{5mm}

  \iftoggle{slovak_used}{
    \mkuctxtt{12}{\@fit@SK}
    \mkuctxtt{9}{\@fit@EN}
  }{
    \iftoggle{enslovak_used}{
      \mkuctxtt{12}{\@fit@EN}
      \mkuctxtt{9}{\@fit@SK}
    }{
      \mkuctxtt{12}{\ifczech\@fit@CS\else\@fit@EN\fi}
      \mkuctxtt{9}{\ifczech\@fit@EN\else\@fit@CS\fi}
    }
  }

  \vspace{5mm}

  \iftoggle{slovak_used}{
    \mkuctxtt{12}{\@departmentname@SK}
    \mkuctxtt{9}{\@departmentname@EN}
  }{
    \iftoggle{enslovak_used}{
      \mkuctxtt{12}{\@departmentname@EN}
      \mkuctxtt{9}{\@departmentname@SK}
    }{
      \mkuctxtt{12}{\ifczech\@departmentname@CS\else\@departmentname@EN\fi}
      \mkuctxtt{9}{\ifczech\@departmentname@EN\else\@departmentname@CS\fi}
    }
  }

}{ % !phd_summary
    
  \vspace{10mm}
  
  \iftoggle{slovak_used}{
    \mkuctxtt{18}{\@vut@SK}
    \mkuctxtt{11}{\@vut@EN}
  }{
    \iftoggle{enslovak_used}{
      \mkuctxtt{18}{\@vut@EN}
      \mkuctxtt{11}{\@vut@SK}
    }{
      \mkuctxtt{18}{\ifczech\@vut@CS\else\@vut@EN\fi}
      \mkuctxtt{11}{\ifczech\@vut@EN\else\@vut@CS\fi}
    }
  }

  \vspace{5mm}

  \iftoggle{slovak_used}{
    \mkuctxtt{14}{\@fit@SK}
    \mkuctxtt{11}{\@fit@EN}
  }{
    \iftoggle{enslovak_used}{
      \mkuctxtt{14}{\@fit@EN}
      \mkuctxtt{11}{\@fit@SK}
    }{
      \mkuctxtt{14}{\ifczech\@fit@CS\else\@fit@EN\fi}
      \mkuctxtt{11}{\ifczech\@fit@EN\else\@fit@CS\fi}
    }
  }

  \vspace{5mm}

  \iftoggle{slovak_used}{
    \mkuctxtt{14}{\@departmentname@SK}
    \mkuctxtt{11}{\@departmentname@EN}
  }{
    \iftoggle{enslovak_used}{
      \mkuctxtt{14}{\@departmentname@EN}
      \mkuctxtt{11}{\@departmentname@SK}
    }{
      \mkuctxtt{14}{\ifczech\@departmentname@CS\else\@departmentname@EN\fi}
      \mkuctxtt{11}{\ifczech\@departmentname@EN\else\@departmentname@CS\fi}
    }
  }
  
} % !phd_summary

\vspace{5mm}

\vfill

% Pro název práce na 2 řádky / For the 2-line thesis title
\newtoggle{long_title}
\togglefalse{long_title}

\ifx\@titlelen\@empty
\else
  \toggletrue{long_title}
\fi

% Pro druhý název práce na 2 řádky / For the 2-line second thesis title
\newtoggle{long_sectitle}
\togglefalse{long_sectitle}

\ifx\@sectitlelen\@empty
\else
  \toggletrue{long_sectitle}
\fi

% Pro název práce nad prohlášením na 2 řádky / For the 2-line thesis title above declaration
\newtoggle{long_dectitle}
\togglefalse{long_dectitle}

\ifx\@dectitlelen\@empty
\else
  \toggletrue{long_dectitle}
\fi

\iftoggle{phd_summary}{

  % Název práce / Thesis title
  \iftoggle{long_title}{
    \mkuctxttl{16}{\@titlelen}{\ifslovak\@title@CS\else\ifczech\@title@CS\else\@title@EN\fi\fi}
    \iftoggle{long_sectitle}{
      \mkuctxttls{9}{\@sectitlelen}{\ifslovak\@title@EN\else\ifczech\@title@EN\else\@title@CS\fi\fi}
    }{
      \mkuctxtt{9}{\ifslovak\@title@EN\else\ifczech\@title@EN\else\@title@CS\fi\fi}
    }
  }{
    \mkuctxtt{16}{\ifslovak\@title@CS\else\ifczech\@title@CS\else\@title@EN\fi\fi}
    \mkuctxtt{9}{\ifslovak\@title@EN\else\ifczech\@title@EN\else\@title@CS\fi\fi}
  }
  \vfill

  \mkuctxtt{10}{\ifslovak\@projecttype@SK\else\ifczech\@projecttype@CS\else\@projecttype@EN\fi\fi}
  \iftoggle{slovak_used}{
  \mkuctxtt{8}{\@projecttype@EN}
  }{
  \mkuctxtt{8}{\ifenslovak\@projecttype@SK\else\ifczech\@projecttype@EN\else\@projecttype@CS\fi\fi}
  }

  \vspace{3mm}

  \mktxtt{10}{\MakeUppercase{\ifslovak\@author@SK\else\ifczech\@author@CS\else\@author@EN\fi\fi} \hfill \get@author}
  \iftoggle{slovak_used}{
    \mkuctxtt{8}{\@author@EN}
  }{
    \mkuctxtt{8}{\ifenslovak\@author@SK\else\ifczech\@author@EN\else\@author@CS\fi\fi}
  }

  \vspace{3mm}

  \mktxtt{10}{\MakeUppercase{\ifslovak\@supervisor@SK\else\ifczech\@supervisor@CS\else\@supervisor@EN\fi\fi} \hfill \get@supervisor}
  \iftoggle{slovak_used}{
    \mkuctxtt{8}{\@supervisor@EN}
  }{
    \mkuctxtt{8}{\ifenslovak\@supervisor@SK\else\ifczech\@supervisor@EN\else\@supervisor@CS\fi\fi}
  }

  \vspace{8mm}

  \mktxtt{9}{BRNO \@year}

  \iftwoside
    \newgeometry{left=1.8cm, text={11.0cm,16.5cm}, top=2.5cm}
  \fi

}{ % !phd_summary
    
  % Název práce / Thesis title
  \iftoggle{long_title}{
    \mkuctxttl{18}{\@titlelen}{\ifslovak\@title@CS\else\ifczech\@title@CS\else\@title@EN\fi\fi}
    \iftoggle{long_sectitle}{
      \mkuctxttls{10}{\@sectitlelen}{\ifslovak\@title@EN\else\ifczech\@title@EN\else\@title@CS\fi\fi}
    }{
      \mkuctxtt{10}{\ifslovak\@title@EN\else\ifczech\@title@EN\else\@title@CS\fi\fi}
    }
  }{
    \mkuctxtt{18}{\ifslovak\@title@CS\else\ifczech\@title@CS\else\@title@EN\fi\fi}
    \mkuctxtt{10}{\ifslovak\@title@EN\else\ifczech\@title@EN\else\@title@CS\fi\fi}
  }
  \vfill

  \mkuctxtt{14}{\ifslovak\@projecttype@SK\else\ifczech\@projecttype@CS\else\@projecttype@EN\fi\fi}
  \iftoggle{slovak_used}{
    \mkuctxtt{10}{\@projecttype@EN}
  }{
    \mkuctxtt{10}{\ifenslovak\@projecttype@SK\else\ifczech\@projecttype@EN\else\@projecttype@CS\fi\fi}
  }

  \vspace{5mm}

  \mktxtt{14}{\MakeUppercase{\ifslovak\@author@SK\else\ifczech\@author@CS\else\@author@EN\fi\fi} \hfill \get@author}
  \iftoggle{slovak_used}{
    \mkuctxtt{10}{\@author@EN}
  }{
    \mkuctxtt{10}{\ifenslovak\@author@SK\else\ifczech\@author@EN\else\@author@CS\fi\fi}
  }

  \vspace{5mm}

  \mktxtt{14}{\MakeUppercase{\ifslovak\@supervisor@SK\else\ifczech\@supervisor@CS\else\@supervisor@EN\fi\fi} \hfill \get@supervisor}
  \iftoggle{slovak_used}{
    \mkuctxtt{10}{\@supervisor@EN}
  }{
    \mkuctxtt{10}{\ifenslovak\@supervisor@SK\else\ifczech\@supervisor@EN\else\@supervisor@CS\fi\fi}
  }

  \vspace{10mm}

  \mktxtt{11}{BRNO \@year}

  \iftwoside
    \newgeometry{left=3.46cm, text={15.2cm,23.0cm}, top=2.96cm}
  \fi

} % !phd_summary

%zadani / assignment
\iftoggle{zadaniTwop}{
  \cleardoublepage
  \includepdf[pages=-,offset=-0.1cm -0.2cm]{zadani.pdf}
  %\ifzadani\includepdf[pages=-,offset=0.6cm -1.7cm]{assignment.pdf}\fi
}{
  \ifzadani
    \includepdf[pages=-,offset=0.6cm -1.7cm]{zadani.pdf}
    %\ifzadani\includepdf[pages=-,offset=0.6cm -1.7cm]{assignment.pdf}\fi
  \fi
}


\ifODSAZ
  \setlength{\parindent}{0pt}
  \setlength{\parskip}{0.5\bigskipamount}
\else
  \setlength\parindent{17\p@}
  \setlength{\parskip}{0pt}
\fi

\iftwoside
  \cleardoublepage
  \pagestyle{plain}
\else
  \newpage
\fi
\fontfamily{\familydefault}\selectfont%

%=================================================================================
\makeabstract
\ifx\@textendedabstract\@empty
\else
  \makeextendedabstract
\fi

\newtoggle{declaration_page}
\togglefalse{declaration_page}
\ifx\@declaration\@empty
\else
  \toggletrue{declaration_page}
\fi
\ifx\undefined\@acknowledgment
\else
  \toggletrue{declaration_page}
\fi
\iftoggle{declaration_page}{
  \makedeclaration
}{}

\clearpage
\setcounter{tocdepth}{2}\setcounter{page}{1}
}

%-----------------------------------------------------------------------------
\def\uv#1{\iflanguage{english}{``#1''}%
                              {\quotedblbase #1\textquotedblleft}}%
%\catcode`\"=\active
%\def"{\bgroup
%    \iflanguage{english}%
%        {``\def"{''\egroup}}%
%        {\quotedblbase\def"{\textquotedblleft\egroup}}}

% Now, switch on what is appropriate for czech:

% czech quotation marks
% \bq - begin quotation, \eq - end quotation
\def\bq{\mbox{\kern.1ex\protect\raisebox{-1.3ex}[0pt][0pt]{''}\kern-.1ex}}
\def\eq{\mbox{\kern-.1ex``\kern.1ex}}
%\setlanguage{\czech}

{%                                      % Begin a group for which " is active
\catcode`\"=\active                     % Make " an active character
\catcode`\@=11                          % Make @ an active character
%
%  \csdoublequoteson
%
%       This macro makes " an active character, resets the control sequence
%       \dblqu@te to L (left), and defines \dq as a replacement for ".
%
\gdef\csdoublequoteson{%                % \csdoublequoteson enables "
    \gdef"{\czechquotes}%               % Define " as \czechquotes
    \global\catcode`\"=\active%         % Make " an active character
    \global\chardef\dq=`\"%             % Double-quote char. via \dq
    \global\let\dblqu@te=L%             % Always start with a left double-quote
    }                                   % End of macro
%
%  \bq, \eq
%
%      These macros define default characters for czech left and right
%      double quotes. Czech opening quote is created from two commas with
%      kerning depending on fontdimen four parameter of current font.
%      Better solution should be specially designed character with
%      proper kernings; if you have such characters in fonts
%      (e.g. in DC-fonts), use it instead. (e.g. define
%      macros \bq and \eq e.g. \def\bq{\char"130 }
%      in your document/style file-- not in csquote.sty!)
%      Similar solution is used for czech right quote.
%
%      \cs existence test, stolen from TeXbook exercise 7.7
\def\ifundefined#1{\expandafter\ifx\csname#1\endcsname\relax }%
%
%      another macro to be more efficient in time and space
\global\chardef\f@@r=4
%
\ifundefined{bq}%
\gdef\bq{\kern-.25\fontdimen\f@@r\font,\kern-.8\fontdimen\f@@r\font,%
                \kern-.35\fontdimen\f@@r\font}%
\fi
\ifundefined{eq}%
\gdef\eq{\kern-.35\fontdimen\f@@r\font`\kern-.8\fontdimen\f@@r\font`%
                \kern-.25\fontdimen\f@@r\font}
\fi
%
% Macro \uv for other usage of \bq and \eq.
%
\ifundefined{uv}\iflanguage{czech}%
        \gdef\uv#1{\bq #1\eq}
\fi
%
% \testquotes macro gives warning if citation span this place
%
\gdef\testquotes{\if R\dblqu@te
        \message{Warning: You forgot right double quote!}%
        \let\dblqu@te=L\fi}
%
%  Define the macro that will be executed whenever " is encountered.
%
\gdef\czechquotes{\protect\czechqu@tes}
\gdef\czechqu@tes{%
        %  If the double-quote is the first character in a new paragraph,
        %  make sure it becomes a left double-quote.  This case can be
        %  detected by checking to see if TeX is currently in vertical mode.
        %  If so, the double-quote is at the beginning of the paragraph
        %  (since " hasn't actually generated any horizontal mode tokens
        %  yet, TeX is still in vertical mode).  If the mode is vertical,
        %  set \dblqu@te equal to L.
        %
        \ifinner\else\ifvmode\testquotes\fi\fi%
        %
        %  Now insert the appropriate left or right double-quote.
        %
        %  If \dblqu@te is L, insert an opening quote and set \dblqu@te to R.
        %
        \if L\dblqu@te\bq\global\let\dblqu@te=R%
        %
        %  Otherwise, save the current \spacefactor, insert '', set \dblqu@te
        %  to L, and reset the original \spacefactor.
        %
        \else%
           \let\xxx=\spacefactor%               % Save the \spacefactor
           \eq%                                 % Insert ending quote
           \global\let\dblqu@te=L%              % and reset \dblqu@te
           \spacefactor\xxx%                    % Reset the \spacefactor
        \fi%                                    % End of \if L\dblqu@te...
        }                                       % End of " macro
}                                               % End of group

\gdef\csdoublequotesoff{%
        \catcode`\"=12%                         % Set " back to other
        }
%
% Czech quotes are default
%
\csdoublequoteson

% Zarovnání v seznamu typu description 
% Alignment in description list
% http://tex.stackexchange.com/questions/7008/change-hanging-indent-in-description-list
\makeatletter
\newcommand{\DESCRIPTION@original@item}{}
\let\DESCRIPTION@original@item\item
\newcommand*{\DESCRIPTION@envir}{DESCRIPTION}
\newlength{\DESCRIPTION@totalleftmargin}
\newlength{\DESCRIPTION@linewidth}
\newcommand{\DESCRIPTION@makelabel}[1]{\llap{#1}}%
\newcommand{\DESCRIPTION@item}[1][]{%
  \setlength{\@totalleftmargin}%
       {\DESCRIPTION@totalleftmargin+\widthof{\textbf{#1 }}-\leftmargin}%
  \setlength{\linewidth}
       {\DESCRIPTION@linewidth-\widthof{\textbf{#1 }}+\leftmargin}%
  \par\parshape \@ne \@totalleftmargin \linewidth
  \DESCRIPTION@original@item[\textbf{#1}]%
}
\newenvironment{DESCRIPTION}
  {\list{}{\setlength{\labelwidth}{0cm}%
           \let\makelabel\DESCRIPTION@makelabel}%
   \setlength{\DESCRIPTION@totalleftmargin}{\@totalleftmargin}%
   \setlength{\DESCRIPTION@linewidth}{\linewidth}%
   \renewcommand{\item}{\ifx\@currenvir\DESCRIPTION@envir
                           \expandafter\DESCRIPTION@item
                        \else
                           \expandafter\DESCRIPTION@original@item
                        \fi}}
  {\endlist}
\makeatother


% Balíčky
% Packages
\usepackage[czech,slovak,english]{babel}
\usepackage[]{opensans}
\usepackage{float}
\usepackage{calc}
\usepackage{longtable,booktabs}
\usepackage{xcolor,colortbl}
\usepackage{multirow}
\usepackage{tabularx}
\usepackage{graphicx}
\usepackage{url}
\DeclareUrlCommand\url{\def\UrlLeft{<}\def\UrlRight{>} \urlstyle{tt}}
\def\UrlBreaks{\do\/\do-}

\usepackage{blindtext}
\usepackage{listings}
\usepackage[toc,page,header]{appendix}
\RequirePackage{titletoc}
\ifczech
  \usepackage{ae}
\fi

%--------------------------------------------------------
% Oprava pro Čechy a Slováky -- vyžaduje aktuální balíčky!
% Fix for CZECH and SLOVAK -- needs up to date packages!
% http://tex.stackexchange.com/questions/111999/slovak-and-czech-babel-gives-problems-with-cmidrule-and-cline
%--------------------------------------------------------
\usepackage{regexpatch}
%\ifczechslovak
  \makeatletter
  % Change the `-` delimiter to an active character
  %\xpatchparametertext\@@@cmidrule{-}{\cA-}{}{}
  %\xpatchparametertext\@cline{-}{\cA-}{}{}
    \begingroup
      \toks0=\expandafter{\@cline{#1}-{#2}\@nil}
      \@ifpackageloaded{booktabs}{%
        \toks2=\expandafter{\@@@cmidrule[{#1}-{#2}]{#3}{#4}}%
      }{}
      \catcode`-=\active
      \edef\x{\gdef\unexpanded{\@cline#1-#2\@nil}{\the\toks0}}\x
      \@ifpackageloaded{booktabs}{%
        \edef\x{\gdef\unexpanded{\@@@cmidrule[#1-#2]#3#4}{\the\toks2}}\x
      }{}
    \endgroup
  \makeatother
%\fi

\ifczechslovak
  \renewcommand{\lstlistingname}{Výpis} %Výpis místo Listing
\fi


% BEGIN Jan Pluskal (ipluskal) listings

\definecolor{bluekeywords}{rgb}{0.13,0.13,1}
\definecolor{greencomments}{rgb}{0,0.5,0}
\definecolor{redstrings}{rgb}{0.9,0,0}

\definecolor{gray}{rgb}{0.4,0.4,0.4}
\definecolor{darkblue}{rgb}{0.0,0.0,0.6}
\definecolor{cyan}{rgb}{0.0,0.6,0.6}

\lstset{
  basicstyle=\ttfamily,
  columns=fullflexible,
  showstringspaces=false,
  commentstyle=\color{gray}\upshape,
  captionpos=b
}

\lstdefinestyle{csh}{language=[Sharp]C,
  showspaces=false,
  showtabs=false,
  breaklines=false,
  showstringspaces=false,
  breakatwhitespace=true,
  escapeinside={(*@}{@*)},
  commentstyle=\color{greencomments},
  keywordstyle=\color{bluekeywords},
  stringstyle=\color{redstrings},
  basicstyle=\ttfamily,
  captionpos=b,
  numbers=left,
  frame=single,
}

\lstdefinelanguage{XML}
{
  morestring=[b]",
  morestring=[s]{>}{<},
  morecomment=[s]{<?}{?>},
  stringstyle=\color{black},
  identifierstyle=\color{darkblue},
  keywordstyle=\color{cyan},
  morekeywords={xmlns,version,type} % list your attributes here
}

\lstdefinestyle{npl}{language=c,
  showspaces=false,
  showtabs=false,
  breaklines=true,
  showstringspaces=false,
  breakatwhitespace=true,
  escapeinside={(*@}{@*)},
  commentstyle=\color{greencomments},
  keywordstyle=\color{bluekeywords},
  stringstyle=\color{redstrings},
  basicstyle=\ttfamily,
  captionpos=b,
  numbers=left,
  frame=single,
  morekeywords={UINT8, UINT16, UINT32, INT8, INT16, INT32, Post, Properties, Protocol, AsciiString, String, StringTerm, BLOB, FormatString, this},
}


\lstdefinestyle{c++}{language=c,
  showspaces=false,
  showtabs=false,
  breaklines=true,
  showstringspaces=false,
  breakatwhitespace=true,
  escapeinside={(*@}{@*)},
  commentstyle=\color{greencomments},
  keywordstyle=\color{bluekeywords},
  stringstyle=\color{redstrings},
  basicstyle=\ttfamily,
  captionpos=b,
  numbers=left,
  frame=single,
  morekeywords={UINT8, UINT16, UINT32, INT8, INT16, INT32, Post, Properties, Protocol, AsciiString, String, StringTerm, BLOB, FormatString, this},
}

%END Jan Pluskal (ipluskal) listings


% Příkazy pro TODO (3 alternativy - lze zvolit preferovanou)
% commands for TODO (3 alternatives - preferred one can be selected)
\newcommand{\todo}[1]{\textcolor{red}{\textbf{[[#1]]}}}
%\usepackage[colorinlistoftodos,prependcaption,textsize=tiny, disable]{todonotes}
%\usepackage[colorinlistoftodos,prependcaption,textsize=tiny]{todonotes}

\newcommand{\phony}[1]{\textcolor{gray}{#1}}
\newcommand{\blind}[1]{\textcolor{gray}{\Blindtext[#1]}}


% Sloupec zarovnaný vlevo s pevnou šířkou
% Left aligned column with a fixed width
\newcolumntype{L}[1]{>{\RaggedRight\hspace{0pt}}p{#1}}


\endinput
%%
%% End of file `fitthesis.cls'.
