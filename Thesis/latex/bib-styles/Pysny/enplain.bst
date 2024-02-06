%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Project:     IBP - Bachelor's Thesis
% Title:       BibTeX style for CSN ISO 690 and CSN ISO 690-2
% Author:      Radek Pysny, xpysny00
% Translated:  Jaroslav Dytrych
% URI:         http://www.fit.vutbr.cz/study/DP/BP.php?id=7848
%
% File:        enplain.pst was created by translating czplain.bst, that was created by editing plain.bst
% Date:        Created on June 31, 2019.
%              Last edited 10. kveten 2009, 16:55.
%              Edited by Adam Herout for FIT thesis template: 31.7.2019
%              Edited by Jaroslav Dytrych to achieve conformance with
%                standard for FIT thesis template: 10.10.2019
% Description: English bibliographic style.
% Use:
% Encoding:    UTF-8
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%% OPTIONS FOR BIBLIOGRAPHIC STYLE SETTINGS %%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% -- Maximum number of authors to be processed.
% If the author element contains a list with more authors than opt.aa, 
% the bibliographic citation will display exactly opt.aa authors. 
% This author listing will end with a string set in opt.etal.
% FUNCTION {opt.aa} { #3 }
% FUNCTION {opt.aa} { #4 }
FUNCTION {opt.aa} { #5 }
% FUNCTION {opt.aa} { #99 }


% -- Maximum number of authors to be processed.
% Similar to option opt.aa.
% FUNCTION {opt.ae} { #1 }
% FUNCTION {opt.ae} { #2 }
% FUNCTION {opt.ae} { #3 }
% FUNCTION {opt.ae} { #4 }
FUNCTION {opt.ae} { #5 }


% -- Separator for the list of authors.
% Used to join names in the list. An exception to this are the last two names, 
% that are joined by a string specified in option opt.sep.ln.
FUNCTION {opt.sep.bn} { ", " }
% FUNCTION {opt.sep.bn} { " -- " }


% -- Separator for the last two names in the list of authors.
FUNCTION {opt.sep.ln} { " and~" }
% FUNCTION {opt.sep.ln} { " -- " }

% -- Unfinished listing of names.
% Used if the number of authors exceeds the limit set by options opt.aa or opt.ae.
% It is also used, if the keyword "others" is used instead of the last author.
FUNCTION {opt.etal} { "et~al." }

% -- Signify editor (redactor).
% Names of editors are distinguished from authors with this option.
FUNCTION {opt.ed} { "ed." }
% FUNCTION {opt.ed} { "(ed.)" }
% FUNCTION {opt.ed} { "(red.)" }
% FUNCTION {opt.ed} { "" }

% -- Signify editor (redactor) after each name.
% Signifies where option opt.ed should be used. Either after each name, 
% or only at the end of the element.
FUNCTION {opt.ed.all} { #0 }     % pouze na konci prvku
% FUNCTION {opt.ed.all} { #1 }    % za kazdym jmenem

% -- Separator between primary author(s) and title elements.
FUNCTION {opt.sep.a} { "." }
% FUNCTION {opt.sep.a} { ":" }

% -- Separator between place of publication and publisher elements
% (alternatively university or another institution).
FUNCTION {opt.sep.p} { ": " }
% FUNCTION {opt.sep.p} { " : " }
% FUNCTION {opt.sep.p} { ", " }

% -- Separator between title and subtitle elements.
% If both title and subtitle elements are used, this string is used as a separator 
% between these two elements.
FUNCTION {opt.sep.t} { ": " }
% FUNCTION {opt.sep.t} { " : " }

% -- Oznaceni rozsahu u akademickych praci.
% U akademickych praci mate pomoci teto volby moznost urcit, zda-li bude rozsah
% techto praci udavan ve stranach nebo v listech.

% -- Specifies number of pages of a theses.
% Used for theses, you can choose between number of pages and number of sheets.
FUNCTION {opt.pages} { "p." }    % number of pages
% FUNCTION {opt.pages} { "sh." }   % number of sheets


% -- Beginning of the availability element (url).
% This option affects text, that is displayed before the URL itself.
% FUNCTION {opt.url} { "" }
FUNCTION {opt.url} { "Available at: " }
% FUNCTION {opt.url} { "Available from: " }

FUNCTION {opt.path} { "Path: " }

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%% DECLARATION OF ENTRIES AND VARIABLES %%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Entries that are accepted and processed by this style.
ENTRY
  { address
    author
    booktitle
    booksubtitle    % book subtitle
    cited           % date of citation
    contributory    % secondary author(s)
    day
    doi
    edition
    editionnumber   % edition number
    editor
    howpublished    % type of medium
    chapter
    institution
    inserts         % number of appdendices pages
    isbn            % standard number
    issn            % standard number
    journal
    journalsubtitle
    key
    location
    month
    note
    number
    organization
    path
    pages
    publisher
    revised         % revision date/update
    secondarytitle  % webpage title
    school
    series          % edition name
    subtitle        % subtitle
    supervisor      % for theses
    time            % publication time
    title
    type
    url             % availability
    version         % version of electronic documents
    volume
    year
  }
  { }
  { label }

% Integer variables. Each variable (except for constants, that are only initialized 
% in function init.state.consts -- before.all, mid.sentence, after.sentence,
% after.block) has a list of functions, that modify its value.
%
% output.state -- output.nonnull, output.bibitem, new.block, new.sentance
%
% ord -- is.ord
%
% ptr, i, x -- str.to.int
%
% numnames, namesleft, nameptr -- format.names, format.names.ed,
%                                 sort.format.names
INTEGERS { output.state before.all mid.sentence after.sentence after.block
           ord ptr i x numnames namesleft nameptr }

% String variables. Each variable has a list of functions, that modify its value.
%
% s -- output.nonnull, format.names, format.names.ed, format.journal.issue,
%      format.thesis.info, format.thesis.range, chop.word, sort.format.names
%
% t -- output.check, dashify, str.to.int, format.names, format.names.ed,
%      format.full.date, sort.format.names, sort.format.title
%
% u -- is.ord, tie.or.connnect, comma.connect
STRINGS { s t u }

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%% DEFINITION OF ABBREVIATIONS -- ENGLISH NAMES OF MONTHS %%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

MACRO {jan} {"january"}

MACRO {feb} {"february"}

MACRO {mar} {"march"}

MACRO {apr} {"april"}

MACRO {may} {"may"}

MACRO {jun} {"june"}

MACRO {jul} {"july"}

MACRO {aug} {"august"}

MACRO {sep} {"september"}

MACRO {oct} {"october"}

MACRO {nov} {"november"}

MACRO {dec} {"december"}

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%% SLIGHTLY EDITED FUNCTIONS TAKEN FROM plain.bst %%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Initialization of constants necessary for correct functionality of the style.
%
% void init.state.consts ()
% {
%   int before.all = 0;
%   int mid.sentence = 1;
%   int adter.sentence = 2;
%   int after.block = 3;
% }
FUNCTION {init.state.consts}
{ #0 'before.all :=
  #1 'mid.sentence :=
  #2 'after.sentence :=
  #3 'after.block :=
}

% Output of preformatted array, that is certainly not empty. Output format is a
% ffected by output state (i.e. state variable output.state). The output itself
% is delayed (the current value is printed upon the next call of this function)
% as the correct separator needs to be chosen (dot or comma).
%
% void output.nonnull ()
% {
%   s = pop();
%   if (output.state == mid.sentence)
%   {
%     write(pop() * " ");    // zde byla carka!!!
%   } else
%   {
%     if (output.state == after.block)
%     {
%       write(add.period(pop()));
%       flush();
%       write("\newblock ");
%     } else
%     {
%       if (output.state == before.all)
%       {
%         write(pop());
%       } else
%       {
%         write(add.period(pop()) * " ");
%       }
%     }
%   }
%   push(s);
% }
FUNCTION {output.nonnull}
{ 's :=
  output.state mid.sentence =
    { " " * write$ }                %% Uprava: Nahrada ", " za " "!
    { output.state after.block =
        { add.period$ write$
          newline$
          "\newblock " write$
        }
        { output.state before.all =
            { write$ }
            { add.period$ " " * write$ }
          if$
        }
      if$
      mid.sentence 'output.state :=
    }
  if$
  s
}

% If a nonnull value is at the top of stack, the output is executed with 
% the output.nonnull function. Otherwise it is removed from stack.
%
% /** pseudocode without the use of a stack **/
% void output (string string.to.write)
% {
%   if (empty$(string.to.write))
%   { } else
%   {
%     output.nonnull(string.to.write);
%   }
% }
FUNCTION {output}
{ duplicate$ empty$
  { pop$ }
  { output.nonnull }
  if$
}

% If a nonnull value is at the top of stack, the output is executed with
% the output.nonnull function. Otherwise it is removed from stack and warning
% regarding missing value is printed to error output. It is used to inform the 
% user of a missing value that % is required by the czech standard.
%
% /** pseudocode without the use of a stack **/
% void output.check (string type.of.field string string.to.write)
% {
%   t = type.of.field;
%   if (empty$(string.to.write))
%   {
%     warning$("empty" * t * " in " * cite$())
%   } else
%   {
%     output.nonnull(string.to.write);
%   }
% }
FUNCTION {output.check}
{ 't :=
  duplicate$ empty$
    { pop$ "empty " t * " in " * cite$ * warning$ }
    { output.nonnull }
  if$
}

% Prints the beginning of a bibliographic citation.
%
% void output.bibitem ()
% {
%   newline$();
%   write$("\bibitem{");
%   write$(cite$);
%   write$("}");
%   newline$();
%   push("");
%   int output.state = before.all;
% }
FUNCTION {output.bibitem}
{ newline$
  "\bibitem{" write$
  cite$ write$
  "}" write$
  newline$
  ""
  before.all 'output.state :=
}

% Entry completion (with a dot at the end).
%
% void fin.entry ()
% {
%   write(add.period$(pop()));
%   newline$();
% }
FUNCTION {fin.entry}
{ add.period$ write$
  newline$
}

% State change: {mid.sentence; after.sentence} => after.block.
%
% void new.block ()
% {
%   if (output.state == before.all)
%   { } else
%   {
%     output.state = after.block;
%   }
% }
FUNCTION {new.block}
{ output.state before.all =
    'skip$
    { after.block 'output.state := }
  if$
}

% State change: mid.sentence => after.sentence.
%
% void new.sentence ()
% {
%   if (output.state == after.block)
%   { } else
%   {
%     if (output.state == before.all)
%     { } else
%     {
%       output.state = after.sentence;
%     }
%   }
% }
FUNCTION {new.sentence}
{ output.state after.block =
  'skip$
  { output.state before.all =
    'skip$
    { after.sentence 'output.state := }
    if$
  }
  if$
}

% Negation of condition.
%
% int not (int bool)
% {
%   if (bool)
%   {
%     return 0; // false
%   } else
%   {
%     return 1; // true
%   }
% }
FUNCTION {not}
{ { #0 }
  { #1 }
  if$
}

% Conjunction of two conditions.
%
% int and (int bool1, int bool2)
% {
%   if (bool2)
%   {
%     return bool1;
%   } else
%   {
%     return 0;
%   }
% }
FUNCTION {and}
{ 'skip$
  { pop$ #0 }
  if$
}

% Disjunction of two conditions.
%
% int or (int bool1, int bool2)
% {
%   if (bool2)
%   {
%     return 1;
%   } else
%   {
%     return bool1;
%   }
% }
FUNCTION {or}
{ { pop$ #1 }
  'skip$
  if$
}

% Beginning of a new block if there is a nonnull value at the of stack.
%
% void new.block.checka (int.or.string anything.from.the.top)
% {
%   if (empty$(anything.from.the.top))
%   { } else
%   {
%     new.block();
%   }
% }
FUNCTION {new.block.checka}
{ empty$
  'skip$
  { new.block }
  if$
}

% Beginning of a new block if there are two nonnull values at the top of stack.
%
% void new.block.checkb (int.or.string anything.under.the.top,
%                           int.or.string anything.from.the.top)
% {
%   if ( (empty$(anything.from.the.top)) && (empty$(anything.under.the.top)) )
%   { } else
%   {
%     new.block();
%   }
% }
FUNCTION {new.block.checkb}
{ empty$
  swap$ empty$
  and
  'skip$
  { new.block }
  if$
}

% Beginning of a new sentence if there is a nonnull value at the top of stack.
%
% void new.sentence.checka (int.or.string anything.from.the.top)
% {
%   if (empty$(anything.from.the.top))
%   { } else
%   {
%     new.sentence();
%   }
% }
FUNCTION {new.sentence.checka}
{ empty$
  'skip$
  { new.sentence }
  if$
}

% Beginning of a new sentence if there are two nonnull values at the top of stack.
%
% void new.sentence.checkb (int.or.string anything.under.the.top,
%                           int.or.string anything.from.the.top)
% {
%   if ( (empty$(anything.from.the.top)) && (empty$(anything.under.the.top)) )
%   { } else
%   {
%     new.sentence();
%   }
% }
FUNCTION {new.sentence.checkb}
{ empty$
  swap$ empty$ and
  'skip$
  { new.sentence }
  if$
}

% Returns value of an entry (at the top of stack) or empty (null) string.
%
% int.or.string field.or.null (int.or.string anything.from.the.top)
% {
%   if (empty$(anything.from.the.top))
%   {
%     return "";
%   } else
%   {
%     return anything.from.the.top;
%   }
% }
FUNCTION {field.or.null}
{ duplicate$ empty$
  { pop$ "" }
  'skip$
  if$
}

% Prints an error message in cases, where there are two nonnull mutually exclusive entries.
% Function is called with two arguments -- the first one is a string containing names of both
% items and the other one is the content of one of the items.
%
% void either.or.check (string field.names, int.or.string one.of.fields)
% {
%   if (empty$(one.of.fields))
%   { } else
%   {
%     warning$(field.names * "can't use both " * " fields in " * cite$());
%   }
% }
FUNCTION {either.or.check}
{ empty$
  { pop$ }
  { "can't use both " swap$ * " fields in " * cite$ * warning$ }
  if$
}

% Adds emphasis to the value at the top of stack (encloses it with "{\em"
% and "}"). Does not utilize italic type correction.
%
% string emphasize (string x)
% {
%   if (empty$(x))
%   {
%     return "";
%   } else
%   {
%     return "{\em " * x * "}";
%   }
% }
FUNCTION {emphasize}
{ duplicate$ empty$
  { pop$ "" }
  { "{\em " swap$ * "}" * }
  if$
}

FUNCTION {makebold}
{ duplicate$ empty$
  { pop$ "" }
  { "{\bf " swap$ * "}" * }
  if$
}

FUNCTION {makesmall}
{ duplicate$ empty$
  { pop$ "" }
  { "{\small " swap$ * "}" * }
  if$
}

% Replaces each occurence of '-' with "--".
%
% string dashify (string to.dashify)
% {
%   t = to.dashify;
%   push("");
%   while (!empty$(t))
%   {
%     if (substring$(t, 1, 1) == "-")
%     {
%       if (substring$(t, 1, 2) == "--")
%       {
%         while (substring$(t, 1, 1) == "-")
%         {
%           push(pop() * "-");
%           t = substring$(t, 2, global.max$());
%         }
%       } else
%       {
%         push(pop() * "--");
%         t = substring$(t, 2, global.max$());
%       }
%     } else
%     {
%       push(pop() * substring$(t, 1, 1));
%       t = substring$(t, 2, global.max$());
%     }
%   }
% }
FUNCTION {dashify}
{ 't :=
  ""
  { t empty$ not }
  { t #1 #1 substring$ "-" =
   { t #1 #2 substring$ "--" =
     { { t #1 #1 substring$ "-" = }
      { "-" *
        t #2 global.max$ substring$ 't :=
      }
       while$
     }
     { "--" *
       t #2 global.max$ substring$ 't :=
     }
     if$
   }
   { t #1 #1 substring$ *
     t #2 global.max$ substring$ 't :=
   }
    if$
  }
  while$
}

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%% CODE -- AUXILIARY FUNCTIONS %%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Conversion of string to number. A function call is expected after a successful
% test using the 'is.ord' function. The expected input is a nonnull string 
% comprised of digits only.
%
% int str.to.int (string to.convert)
% {
%   t = to.convert;
%   int x = 0;
%   int ptr = 0;
%   while (ptr text.length$(t) < )
%   {
%     push(x);
%     i = 9;
%     while (i > 0)
%     {
%       push(pop() + x);
%       i--;
%     }
%     ptr++;
%     x = chr.to.int$(substring$(t, ptr, 1)) - 48 + pop();
%   }
%   return x;
% }
FUNCTION {str.to.int}
{ 't :=
  #0 'x :=
  #0 'ptr :=
  { ptr t text.length$ < }
  { x
    #9 'i :=
    { i #0 > }
    { x +
      i #1 - 'i :=
    }
    while$
    ptr #1 + 'ptr :=
    t ptr #1 substring$ chr.to.int$ #48 - + 'x :=
  }
  while$
  x
}

% Converts the first letter of a string at the top of stack to capital.
%
% string capitalize (string to.capitalize)
% {
%   if (empty$(to.capitalize))
%   { } else
%   {
%     return change.case$(substring$(to.capitalize, 1, 1), "u") *
%            substring$(to.capitalize, 2, global.max$());
%   }
% }
FUNCTION {capitalize}
{ duplicate$ empty$
  'skip$
  { duplicate$ #1 #1 substring$ "u" change.case$
    swap$ #2 global.max$ substring$ *
  }
  if$
}

% Connects two strings from the top of the stack. These two strings are separated
% by a whitespace or non-breaking space. String at the top of the stack is connected
% to the other one, specifically behind it.
%
% If either of the strings is empty, the other string is returned without any changes.
% If both strings are empty, the function also returns an empty string.
%
% string tie.or.connect (string under.the.top, string from.the.top)
% {
%   string u = from.the.top;
%   if (empty$(under.the.top))
%   {
%     return u;
%   } else
%   {
%     if (empty$(u))
%     { } else
%     {
%       if (text.length$(u) < 3)
%       {
%         return under.the.top * "~" * u;
%       } else
%       {
%         return under.the.top * " " * u;
%       }
%     }
%   }
% }
FUNCTION {tie.or.connect}
{ 'u :=
  duplicate$ empty$
  { pop$ u }
  { u empty$
    'skip$
    { u text.length$ #3 <
      { "~" * u * }
      { " " * u * }
      if$
    }
    if$
  }
  if$
}

% Connects two strings from the top of the stack. These two strings are separated
% by a comma. String at the top of the stack is connected to the other one, 
% specifically behind it.
%
% If either of the strings is empty, the other string is returned without any changes.
% If both strings are empty, the function also returns an empty string.
%
% string comma.connect (string under.the.top, string from.the.top)
% {
%   string u = from.the.top;
%   if (empty$(under.the.top))
%   {
%     return u;
%   } else
%   {
%     if (empty$(u))
%     { } else
%     {
%       return under.the.top * ", " * u;
%     }
%   }
% }
FUNCTION {comma.connect}
{ 'u :=
  duplicate$ empty$
  { pop$ u }
  { u empty$
    'skip$
    { ", " * u * }
    if$
  }
  if$
}

% Connects two strings from the top of the stack. A whitespace is used as the separator.
FUNCTION {space.connect}
{ 'u :=
  duplicate$ empty$
  { pop$ u }
  { u empty$
    'skip$
    { " " * u * }
    if$
  }
  if$
}


% Tests, whether or not a value at the top of stack is comprised of digits only.
%
% int is.ord (string x)
% {
%   string u = x;
%   int ord = 1;
%   while ( (ord) && (!empty(u)) )
%   {
%     if ( (chr.to.int$(substring$(u, 1, 1)) < 48) ||
%          (chr.to.int$(substring$(u, 1, 1)) > 57) )
%     {
%       ord = 0;
%     } else
%     {
%       u = substring(s, 2, global.max$());
%     }
%   }
%   return ord;
% }
FUNCTION {is.ord}
{ 'u :=
  #1 'ord :=
  { ord
    u empty$ not
    and
  }
  { u #1 #1 substring$
    duplicate$ chr.to.int$ #48 <  % < '0'
    swap$ chr.to.int$ #57 >       % > '9'
    or
    { #0 'ord := }
    { u #2 global.max$ substring$ 'u := }
    if$
  }
  while$
  ord
}

% Formatting string for insertion of names according to czech convention
% using the 'format.name$' function.
%
% string cz.name.format ()
% {
%   return "{{\scshape\bgroup}ll{ }{\egroup}}{, f.}{ vv}";
% }
FUNCTION {cz.name.format}
{ "{{\sc\bgroup}ll{ }{\egroup}}{, f.}{ vv}" }

% Vraci nominativ mesice.
%
% /** prepis pseudokodu do funkce pracujici s polem **/
% string get.month.n (int n)
% {
%   int month = { 1 => "january", "february", "march", "april", "may", june",
%                "july", "august", "september", "october", "november", "december" };
%   if ( (n > 0) && (n < 13) )
%   {
%     return month[n];
%   } else
%   {
%     warning$("Month must be between 1 and 12!");
%     return "";
%   }
% }
FUNCTION {get.month.n}
{ duplicate$ #1 =
  { "january" swap$ pop$ }
  { duplicate$ #2 =
    { "february" swap$ pop$ }
    { duplicate$ #3 =
      { "march" swap$ pop$ }
      { duplicate$ #4 =
        { "april" swap$ pop$ }
        { duplicate$ #5 =
          { "may" swap$ pop$ }
          { duplicate$ #6 =
            { "june" swap$ pop$ }
            { duplicate$ #7 =
              { "july" swap$ pop$ }
              { duplicate$ #8 =
                { "august" swap$ pop$ }
                { duplicate$ #9 =
                  { "september" swap$ pop$ }
                  { duplicate$ #10 =
                    { "october" swap$ pop$ }
                    { duplicate$ #11 =
                      { "november" swap$ pop$ }
                      { duplicate$ #12 =
                        { "december" swap$ pop$ }
                        { "" "Month must be between 1 and 12!" warning$ }
                        if$
                      }
                      if$
                    }
                    if$
                  }
                  if$
                }
                if$
              }
              if$
            }
            if$
          }
          if$
        }
        if$
      }
      if$
    }
    if$
  }
  if$
}

% Vraci genitiv mesice.
%
% /** prepis pseudokodu do funkce pracujici s polem **/
% string get.month.g (int n)
% {
%   int month = { 1 => "january", "february", "march", "april", "may", "june",
%                "july", "august", "september", "october", "november", "december"};
%   if ( (n > 0) && (n < 13) )
%   {
%     return month[n];
%   } else
%   {
%     warning$("Month must be between 1 and 12!");
%     return "";
%   }
% }
FUNCTION {get.month.g}
{ duplicate$ #1 =
  { "January" swap$ pop$ }
  { duplicate$ #2 =
    { "February" swap$ pop$ }
    { duplicate$ #3 =
      { "March" swap$ pop$ }
      { duplicate$ #4 =
        { "April" swap$ pop$ }
        { duplicate$ #5 =
          { "May" swap$ pop$ }
          { duplicate$ #6 =
            { "June" swap$ pop$ }
            { duplicate$ #7 =
              { "July" swap$ pop$ }
              { duplicate$ #8 =
                { "August" swap$ pop$ }
                { duplicate$ #9 =
                  { "September" swap$ pop$ }
                  { duplicate$ #10 =
                    { "October" swap$ pop$ }
                    { duplicate$ #11 =
                      { "November" swap$ pop$ }
                      { duplicate$ #12 =
                        { "December" swap$ pop$ }
                        { "" "Month must be between 1 and 12!" warning$ }
                        if$
                      }
                      if$
                    }
                    if$
                  }
                  if$
                }
                if$
              }
              if$
            }
            if$
          }
          if$
        }
        if$
      }
      if$
    }
    if$
  }
  if$
}

% Use only at the beginning of a bibliographic citation! Can cause switching of 
% elements elsewhere.
%
% void output.authors (string formatted.authors)
% {
%   if (substring$(formatted.authors, #-1, #1) == opt.sep.a())
%   {
%     write$(formatted.authors);
%     newline$();
%     write("\newblock ");
%   } else
%   {
%     write$(formatted.authors * opt.sep.a());
%     newline$();
%     write("\newblock ");
%   }
% }
FUNCTION {output.authors}
{
  duplicate$ #-1 #1 substring$ opt.sep.a =
  { write$ newline$ "\newblock " write$ }
  { opt.sep.a *
    write$ newline$ "\newblock " write$
  }
  if$
}

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%% CODE -- FORMATTING FUNCTIONS %%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Names formatting (primary author(s)).
%
% string format.names (string names)
% {
%   string s = names;
%   int numnames = num.names$(s);
%   int namesleft = numnames;
%   int nameptr = 1;
%   while ( (namesleft > 0) && (nameptr <= opt.aa()) )
%   {
%     t = format.name$(s, nameptr, cz.name.format());
%     if (nameptr == 1)
%     {
%       push(t);
%     } else
%     {
%       if (namesleft > 1)
%       {
%         push(pop() * opt.sep.bn() * t);
%       } else
%       {
%         if (purify$(t) == "others")
%         {
%           push(tie.or.connect(pop(), opt.etal()));
%         } else
%         {
%           push(pop() * opt.sep.ln() * t);
%         }
%       }
%     }
%     nameptr++;
%     namesleft--;
%   }
%   if (nameledt > 0)
%   {
%     push(tie.or.connect(pop(), opt.etal()));
%   }
%   return pop();
% }
FUNCTION {format.names}
{ 's :=                             % s <= item that contains names
  s num.names$ 'numnames :=         % numnames <= number of names in the item
  numnames 'namesleft :=            % stores the number of remaining names
  #1 'nameptr :=                    % nameptr <= pointer to first name
  { namesleft #0 >
    nameptr opt.aa > not
    and
  }
  { % individual name processing cycle
    s nameptr cz.name.format format.name$ 't :=
    nameptr #1 =
    { t }                           % first name
    { namesleft #1 >                % other names
      { opt.sep.bn t * * }
      { t purify$
        "others" =                  % if the "others" keyword remains after purification
        { opt.etal tie.or.connect } % prints et al.
        { opt.sep.ln * t * }        % otherwise last initials follow after the connective "and"
        if$
      }
      if$
    }
    if$
    nameptr #1 + 'nameptr :=        % move pointer to another name
    namesleft #1 - 'namesleft :=    % decrement the number of remaining names
  }
  while$
  namesleft #0 >                    % possible addition of opt.etal
  { opt.etal tie.or.connect }
  'skip$
  if$
}


% Editor names formatting
%
% string format.names.ed (string names)
% {
%   string s = names;
%   int numnames = num.names$(s);
%   int namesleft = numnames;
%   int nameptr = 1;
%   while ( (namesleft > 0) && (nameptr <= opt.aa()) )
%   {
%     t = format.name$(s, nameptr, cz.name.format());
%     if (nameptr == 1)
%     {
%       if (opt.ed.all())
%       {
%         push(tie.or.connect(t, opt.ed());
%       } else
%       {
%         push(t);
%       }
%     } else
%     {
%       if (namesleft > 1)
%       {
%         if (opt.ed.all())
%         {
%           push(tie.or.connect(pop() * opt.sep.bn() * t, opt.ed());
%         } else
%         {
%           push(pop() * opt.sep.bn() * t);
%         }
%       } else
%       {
%         if (purify$(t) == "others")
%         {
%           push(tie.or.connect(pop(), opt.etal()));
%         } else
%         {
%           if (opt.ed.all())
%           {
%             push(tie.or.connect(pop() * opt.sep.ln() * t, opt.ed());
%           } else
%           {
%             push(pop() * opt.sep.ln() * t);
%           }
%         }
%       }
%     }
%     nameptr++;
%     namesleft--;
%   }
%   if (nameledt > 0)
%   {
%     push(tie.or.connect(pop(), opt.etal()));
%   }
%   if (opt.ed.all())
%   {
%     return pop();
%   } else
%   {
%     return tie.or.connect(pop(), ", " * opt.ed());
%   }
% }
FUNCTION {format.names.ed}
{ 's :=                             % s <= item that contains names
  s num.names$ 'numnames :=         % numnames <= number of names in the item
  numnames 'namesleft :=            % stores the number of remaining names
  #1 'nameptr :=                    % nameptr <= pointer to first name
  { namesleft #0 >
    nameptr opt.ae > not
    and
  }
  { % individual name processing cycle
    s nameptr cz.name.format format.name$ 't :=
    nameptr #1 =                    % first name
    { t
      opt.ed.all
      { opt.ed tie.or.connect }
      'skip$
      if$
    }
    { namesleft #1 >                % other names
      { opt.sep.bn t * *
        opt.ed.all
        { opt.ed tie.or.connect }
        'skip$
        if$
      }
      { t purify$
        "others" =                  % if the "others" keyword remains after purification
        { opt.etal tie.or.connect } % prints et al.
        { opt.sep.ln t * *          % otherwise last initials follow after the connective "and"
          opt.ed.all
          { opt.ed tie.or.connect }
          'skip$
          if$
        }
        if$
      }
      if$
    }
    if$
    nameptr #1 + 'nameptr :=        % move pointer to another name
    namesleft #1 - 'namesleft :=    % decrement the number of remaining names
  }
  while$
  namesleft #0 >                    % possible additon of opt.etal
  { opt.etal tie.or.connect }
  'skip$
  if$
  opt.ed.all                        % possible addition of opt.ed at the end of the element
  'skip$
  { ", " * opt.ed tie.or.connect }
  if$
}

% Author names formatting
% If the 'author' element is not empty, applies formatting using the
% 'format.names' function.
%
% string format.authors ()
% {
%   if (empty$(author))
%   {
%     return "";
%   } else
%   {
%     return format.names(author);
%   }
% }
FUNCTION {format.authors}
{ author empty$
  { "" }
  { author format.names }
  if$
}

% Supervisor name formatting
FUNCTION {format.supervisor}
{ supervisor empty$
  { "" }
  { "Supervisor " supervisor format.names * }
  if$
}

% Editor names formatting
% If the 'editor' element is not empty, applies formatting using the
% 'format.names.ed' function.
%
% string format.editors ()
% {
%   if (empty$(editor))
%   {
%     return "";
%   } else
%   {
%     return format.names.ed(editor);
%   }
% }
FUNCTION {format.editors}
{ editor empty$
  { "" }
  { editor format.names.ed }
  if$
}

% Primary author(s) (author) element formatting.
% If the element 'author' is used, its formatting is executed
% by the 'format.authors' function. If the element 'author' is empty,
% its formatting is executed by the 'format.editors' function.
%
% void author.or.editor ()
% {
%   if (empty$(author))
%   {
%     if (empty$(format.editors()))
%     {
%       warning$("empty author and editor in " * cite$());
%     } else
%     {
%       output.authors(format.editors());
%     }
%   } else
%   {
%     output.authors(format.authors());
%   }
% }
FUNCTION {author.or.editor}
{ author empty$
  { format.editors
    duplicate$ empty$
    { pop$
      "empty author and editor in " cite$ * warning$
    }
    { output.authors }
    if$
  }
  { format.authors
    output.authors
  }
  if$
}

% Title formatting (e.g. magazine, journal etc.)
% If the element 'subtitle' is used, it is connected to the contents
% element 'title' using ": ". The title is then properly capitalized.
%
% string format.title ()
% {
%   if (empty$(subtitle))
%   {
%     return capitalize(title);
%   } else
%   {
%     return capitalize(title) * opt.sep.t() * subtitle;
%   }
% }
FUNCTION {format.title}
{ subtitle empty$
  { title capitalize }
  { title capitalize
    opt.sep.t * subtitle *
  }
  if$
}
%%FUNCTION {format.title}
%%{ subtitle empty$
%%    { title "t" change.case$ }
%%    { title "t" change.case$ 
%%      opt.sep.t * subtitle "t" change.case$ *
%%    }
%%  if$
%%}


% Monographic publication title formatting.
% If the 'subtitle' element is used, it is connected to the contents
% of the 'title' element using ": ". Both parts are properly capitalized
% and emphasized. If the 'subtitle' element is empty, the capitalization
% and emphasis is only applied to it.
%
% string format.btitle ()
% {
%   return emphasize(format.title());
% }
FUNCTION {format.btitle}
{ format.title emphasize
}

% Title formatting (e.g. conference proceedings).
% If the 'booksubtitle' element is used, it is connected to the contents
% of the 'booktitle' element using ": ". The title is then properly capitalized
% and emphasized by italic type.
%
% string format.from.btitle ()
% {
%   if (empty$(booktitle))
%   {
%     warning$(empty booktitle in " * cite$());
%     push("");
%   } else
%   {
%     push(capitalize(booktitle));
%     if (empty$(booksubtitle))
%     { } else
%     {
%       push(pop() * opt.sep.t * booksubtitle);
%     }
%   }
%   return emphasize(pop());
% }
FUNCTION {format.from.btitle}
{ booktitle empty$
  { "empty booktitle in " cite$ * warning$
    ""
  }
  { booktitle capitalize
    booksubtitle empty$
    'skip$
    { opt.sep.t * booksubtitle * }
    if$
  }
  if$
  emphasize
}

% Medium type formatting (howpublished).
%
% string format.howpublished ()
% {
%   if (empty$(howpublished))
%   {
%     return "";
%   } else
%   {
%     if (change.case(howpublished, "l") == "cd")
%     {
%       push("CD-ROM");
%     } else
%     {
%       if (change.case(howpublished, "l") == "online")
%       {
%         push("online");
%       } else
%       {
%         push(howpublished);
%       }
%     }
%     return "[" * pop() * "]";
%   }
% }
FUNCTION {format.howpublished}
{ howpublished empty$
  { "" }
  { howpublished "l" change.case$ "cd" =
    { "CD-ROM" }
    { howpublished "l" change.case$ "online" =
      { "online" }
      { howpublished }
      if$
    }
    if$
    "[" swap$ * "]" *
  }
  if$
}

% Edition formatting. //TODO num
%
% string format.edition ()
% {
%   if (empty$(edition))
%   {
%     push("");
%   } else
%   {
%     if (edition = 1 or edition = 21 or edition = 31)
%     {
%       push(edition * "st ed.");
%     } else
%     {
%       if (edition = 2 or edition = 22 or edition = 23)
%       {
%         push(edition * "nd ed.");
%       } else
%       {
%         if (edition = 3 or edition = 23 or edition = 33)
%         {
%           push(edition * "rd ed.");
%         } else
%         {
%           push(edition * "th ed.");
%         }
%       }
%     }
%   }
%   if (!empty$(version))
%   {
%     push(comma.connect(pop(), version));
%   }
%   return pop();
% }
FUNCTION {format.edition}
{ edition empty$
    { "" }
    { edition "1" =
      edition "21" =
      edition "31" =
        or or
        { edition "st ed." * }
        { edition "2" =
          edition "22" =
          edition "32" =
          or or
            { edition "nd ed." * }
            { edition "3" =
              edition "23" =
              edition "33" =
              or or
                { edition "rd ed." * }
                { edition "th ed." * }
              if$ 
            }
          if$ 
        }
      if$
    }
  if$
  howpublished empty$
    'skip$
    { version empty$
        'skip$
        { version comma.connect }
      if$
    }
  if$
}

% Month and year formatting.
%
% string format.date ()
% {
%   if (empty$(month))
%   {
%     if (empty$(year))
%     {
%       warning$("empty year in " * cite$());
%       return "";
%     } else
%     {
%       return year;
%     }
%   } else
%   {
%     if (empty$(year))
%     {
%       warning$("just month (empty year) in " * cite$());
%       return "";
%     } else
%     {
%       if (is.ord(month))
%       {
%         return get.month.n(str.to.int(month)) * " " * year;
%       } else
%       {
%         return month * " " * year;
%       }
%     }
%   }
% }
FUNCTION {format.date}
{ month empty$
    { year empty$
        { "" 
          "empty year in " cite$ * warning$ }
        { year }
      if$
    }
  { year empty$
      { "" 
        "just month (empty year) in " cite$ * warning$ }
      { month duplicate$ is.ord
          { str.to.int get.month.n " " * year * }
          { " " * year * }
        if$
      }
    if$
  }
  if$
}

% Recognition and conversion of date from '!yyyy-mm-dd' format.
%
% string format.full.date (string date)
% {
%   string t = date;
%   if (text.length$(t) == 10)
%   {
%     if ( (is.ord(substring$(t, 1, 4))) && (substring$(t, 5, 1) == "-") &&
%          (is.ord(substring$(t, 6, 2))) && (substring$(t, 8, 1) == "-") &&
%          (is.ord(substring$(t, 9, 2))) )
%     {
%       if (substring$(t, 9, 1) == "0")
%       {
%         if (substring$(t, 10, 1) == "0")
%         {
%           warning$("day must be between 1 and 31 in " * cite$());
%         } else { }
%         push(substring$(t, 10, 1) * ".~");
%       } else
%       {
%         if ( ((chr.to.int$(substring$(t, 9, 1)) - 48) > 3) ||
%              ((substring$(t, 9, 1) == "3") &&
%               ((chr.to.int$(substring$(t, 10, 1)) - 48) > 1)) )
%         {
%           pop();
%           push(t);
%           warning$("day must be between 1 and 31 in " * cite$());
%         } else { }
%         push(substring$(t, 9, 2) * ".~");
%       }
%       if (substring$(t, 6, 1) == "0")
%       {
%         if (substring$(t, 7, 1) == "0")
%         {
%           pop();
%           push(t);
%           warning$("month must be between 1 and 12 in " * cite$());
%         } else
%         {
%           return pop() * get.month.g(chr.to.int$(substring$(t, 7, 1)) - 48) *
%                  " " * substring$(t, 1, 4));
%         }
%       } else
%       {
%         if ( (substring$(t, 6, 1) == "1") &&
%              ((chr.to.int$(substring$(t, 7, 1)) - 48) < 3) )
%         {
%           return pop() * get.month.g(chr.to.int$(substring$(t, 7, 1))
%                  - 48 + 10) * " " * substring$(t, 1, 4));
%         } else
%         {
%           pop();
%           push(t);
%           warning$("month must be between 1 and 12 in " * cite$());
%         }
%       }
%     } else
%     {
%       return t;
%     }
%   } else
%   {
%     return t;
%   }
% }
FUNCTION {format.full.date}
{ 't :=
  t text.length$ #11 =
  { t #1 #1 substring$ "!" =
    t #2 #4 substring$ is.ord
    t #6 #1 substring$ "-" =
    t #7 #2 substring$ is.ord
    t #9 #1 substring$ "-" =
    t #10 #2 substring$ is.ord
    and and and and and
    { t #10 #1 substring$ "0" =
      { t #11 #1 substring$ "0" =
        { "day must be between 1 and 31 in " cite$ * warning$ }
        'skip$
        if$
        t #11 #1 substring$ ".~" *
      }
      { t #10 #1 substring$ chr.to.int$ #48 - #3 >
        t #10 #1 substring$ "3" =
        t #11 #1 substring$ chr.to.int$ #48 - #1 >
        and or
        { pop$ t "day must be between 1 and 31 in " cite$ * warning$ }
        'skip$
        if$
        t #10 #2 substring$ ".~" *
      }
      if$
      t #7 #1 substring$ "0" =
      { t #8 #1 substring$ "0" =
        { pop$ t "month must be between 1 and 12 in " cite$ * warning$ }
        { t #8 #1 substring$ chr.to.int$ #48 - get.month.g *
          " " * t #2 #4 substring$ *
        }
        if$
      }
      { t #7 #1 substring$ "1" =
        t #8 #1 substring$ chr.to.int$ #48 - #3 <
        and
        { t #8 #1 substring$ chr.to.int$ #48 - #10 + get.month.g *
          " " * t #2 #4 substring$ *
        }
        { pop$ t "month must be between 1 and 12 in " cite$ * warning$ }
        if$
      }
      if$
    }
    { t }
    if$
  }
  { t }
  if$
}

% Returns data from revision date of an electronic document.
%
% string format.revised ()
% {
%   if (empty$(howpublished))
%   {
%     return "";
%   } else
%   {
%     if (empty$(revised))
%     {
%        return "";
%     } else
%     {
%       return revised;
%     }
%   }
% }
FUNCTION {format.revised}
{ howpublished empty$
  { "" }
  { revised empty$
    { "" }
    { revised }
    if$
  }
  if$
}

% Formatting data from revision date of an electronic document.
%
FUNCTION {format.cited}
{ cited empty$
    { "" }
    { cited format.full.date
      "[cit. " swap$ * "]" *
    }
  if$
}

% Formatting of publisher information.
%
% string format.publish.info ()
% {
%   if ( (empty$(address)) && (empty$(publisher)) )
%   {
%     string s = "";
%   } else
%   {
%     if (empty$(address))
%     {
%       s = "[b.m.]: ";
%     } else
%     {
%       s = address * ": ";
%     }
%     if (empty$(publisher))
%     {
%       s = s * "[b.n.]";
%     } else
%     {
%       s = s * publisher;
%     }
%   }
%   return s;
% }
FUNCTION {format.publish.info}
{ address empty$
  publisher empty$
  and
  { "" }
  { address empty$
    { "" }
    { address opt.sep.p * }
    if$
    publisher empty$
    { "[b.n.]" * }
    { publisher * }
    if$
  }
  if$
}

% Publication page range formatting.
%
% string format.range ()
% {
%   if (empty$(pages))
%   {
%     return "";
%   } else
%   {
%     return tie.or.connect(pages, "p.");
%   }
% }
FUNCTION {format.range}
{ pages empty$
    { "" }
    { pages "p." tie.or.connect }
  if$
}

% Conference proceedings volume and number formatting
%
% string format.cvolume ()
% {
%   if (empty$(volume))
%   { 
%     push("");
%   } else
%   {
%     if (is.ord(volume))
%     {
%       push("vol. " * volume);
%     } else
%     {
%       push(volume);
%     }
%   }
%   if (empty$(number))
%   {
%     return pop();
%   } else
%   {
%     if (is.ord(number))
%     {
%       return comma.connect(pop(), "no. " * number);
%     } else
%     {
%       return comma.connect(pop(), number);
%     }
%   }
% }
FUNCTION {format.cvolume}
{ volume empty$
  { "" }
  { volume is.ord
    { "vol. " volume * }
    { volume }
    if$
  }
  if$
  number empty$
  { 
    "" comma.connect
  }
  { number is.ord
    { "no. " number * comma.connect }
    { number comma.connect }
    if$
  }
  if$
}


% Conference proceedings or book series and edition number formatting
%
% string format.cseries ()
% {
%   if (empty$(series))
%   {
%     return "";
%   } else
%   {
%     push(capitalize(series));
%     if (empty$(editionnumber))
%     { } else
%     {
%       if (is.ord(editionnumber))
%       {
%         return comma.connect(pop(), "no. " * editionnumber);
%       } else
%       {
%         return comma.connect(pop(), editionnumber);
%       }
%     }
%   }
% }
FUNCTION {format.cseries}
{ series empty$
  { "" }
  { series capitalize
    editionnumber empty$
    'skip$
    { editionnumber is.ord
      { "no. " editionnumber * comma.connect }
      { editionnumber comma.connect }
      if$
    }
    if$
  }
  if$
}


% Availability formatting.
%
% string format.url ()
% {
%   if (empty$(url))
%   {
%     return "";
%   } else
%   {
%     return tie.or.connect(opt.url(), "\small{\url{" * url * ".}}");
%   }
% }
FUNCTION {format.url}
{ url empty$
  { "" }
  { opt.url "{\small\url{" url * "}}" * tie.or.connect }
  if$
}

% Path formatting.
%
% string format.pah ()
% {
%   if (empty$(path))
%   {
%     return "";
%   } else
%   {
%     return opt.path * path;
%   }
% }
FUNCTION {format.path}
{ path empty$
  { "" }
  { opt.path path * }
  if$
}

% ISBN formatting.
%
% string format.isbn ()
% {
%   if (empty$(isbn))
%   {
%     return "";
%   } else
%   {
%     return "ISBN " * isbn;
%   }
% }
FUNCTION {format.isbn}
{ isbn empty$
  { "" }
  { "ISBN " isbn * }
  if$
}

% Serial publication title formatting.
%
% string format.journal ()
% {
%   if (empty$(journal))
%   {
%     return "";
%   } else
%   {
%     return emphasize(journal);
%   }
% }
FUNCTION {format.journal}
{ journal empty$
  { "" }
  { journalsubtitle empty$
      { journal capitalize emphasize }
      { journal capitalize emphasize
        opt.sep.t * journalsubtitle emphasize *}
    if$
  }
  if$
}

% Location in source document formatting.
%
% string format.pages ()
% {
%   if (empty$(pages))
%   {
%     return "";
%   } else
%   {
%     return "p.~" * dashify(pages);
%   }
% }
FUNCTION {format.pages}
{ pages empty$
  { "" }
  { "p.~" pages dashify * }
  if$
}

FUNCTION {format.chapter}
{ chapter empty$
  { "" }
  { "chap.~" chapter * }
  if$
}

% Formatting of date of publication, year, number of publication + revision/update dated
% + date of citation + location in source document (page range).
%
% string format.journal.issue ()
% {
%   string s;
%   if (empty$(format.date()))
%   {
%     s = "";
%   } else
%   {
%     s = format.date();
%   }
%   if (!empty$(volume))
%   {
%     if (is.ord(volume))
%     {
%       s = comma.connect(s, "vol.~" * volume);
%     } else
%     {
%       s = comma.connect(s, volume);
%     }
%   }
%   if (empty$(number))
%   {
%     warning$("empty number in " * cite$);
%   } else
%   {
%     if (is.ord(number))
%     {
%       s = comma.connect(s, "no.~" * number);
%     } else
%     {
%       s = comma.connect(s, number);
%     }
%   }
%   if (s == "")
%   {
%     warning$("empty journal issue info in " * cite$);
%     return "";
%   } else
%   {
%     return s;
%   }
% }
FUNCTION {format.journal.issue}
{ format.date
  duplicate$ empty$
  { pop$ "" 's := }
  { 's := }
  if$
  volume empty$
  'skip$
  { volume is.ord
    { "vol.~" volume *
      s swap$ comma.connect 's :=
    }
    { s volume comma.connect 's := }
    if$
  }
  if$
  number empty$
  { "empty number in " cite$ * warning$ }
  { number is.ord
    { "no.~" number *
      s swap$ comma.connect 's :=
    }
    { s number comma.connect 's := }
    if$
  }
  if$
  s empty$
  { "empty journal issue info in " cite$ * warning$
    ""
  }
  { s }
  if$
}

% ISSN formatting.
%
% string format.issn ()
% {
%   if (empty$(issn))
%   {
%     return "";
%   } else
%   {
%     return "ISBN " * issn;
%   }
% }
FUNCTION {format.issn}
{ issn empty$
  { "" }
  { "ISSN " issn * }
  if$
}

% DOI formatting.
%
% string format.doi ()
% {
%   if (empty$(doi))
%   {
%     return "";
%   } else
%   {
%     return "DOI: " * doi;
%   }
% }
FUNCTION {format.doi}
{ doi empty$
  { "" }
  { "DOI: " doi * }
  if$
}

% Volume number formatting -- identifying a section for @InBook.
%
% string format.vol ()
% {
%   if (empty$(volume))
%   {
%     push("");
%   } else
%   {
%     if (is.ord(volume))
%     {
%       push("vol.~" * volume);
%     } else
%     {
%       push(volume);
%     }
%   }
%   return capitalize(pop());
% }
FUNCTION {format.vol}
{ volume empty$
  { "" }
  { volume is.ord
    { "vol.~" volume * }
    { volume }
    if$
  }
  if$
  capitalize
}

% Formats basic information (author and title) about conference proceedings for 
% bibliographic citation of a paper in said proceedings.
%
% In <author> opt.sep.a() <proceedings title>.
%
% If <proceedings title> is empty, nothing is printed out.
%
% void conference.basics ()
% {
%   if (empty$(editor))
%   {
%     if (empty$(organization))
%     {
%       warning$("empty editor and organization in " * cite$());
%       push("");
%     } else
%     {
%       if ((substring$(organization, -1, 1) == opt.sep.a())
%       {
%         push(capitalize(organization));
%       } else
%       {
%         push(capitalize(organization * opt.sep.a()));
%       }
%     }
%   } else
%   {
%     if ((substring$(format.editors(), -1, 1) == opt.sep.a())
%     {
%       push(format.editors());
%     } else
%     {
%       push(capitalize(format.editors() * opt.sep.a()));
%     }
%   }
%   push(tie.or.connect("In:", pop()));
%   if (empty$(format.from.btitle()))
%   {
%     pop();
%     push("");
%   } else
%   {
%     push(pop() * format.from.btitle());
%   }
%   return pop();
% }
FUNCTION {conference.basics}
{ editor empty$
  { organization empty$
    { "empty editor and organization in " cite$ * warning$
      ""
    }
    { organization capitalize
      duplicate$ #-1 #1 substring$ opt.sep.a =
      'skip$
      { opt.sep.a * }
      if$
    }
    if$
  }
  { format.editors
    duplicate$ #-1 #1 substring$ opt.sep.a =
    'skip$
    { opt.sep.a * }
    if$
  }
  if$
  "In:" swap$ tie.or.connect
  format.from.btitle
  duplicate$ empty$
  { pop$ }
  { tie.or.connect }
  if$
}


% Formats information about a thesis (bachelor's, dissertation etc).
% The elements used to create a bibliographic citation are different from
% regular publishing information.
%
% string format.thesis.info ()
% {
%   string s = "";
%   if ( (empty$(address)) && (empty$(location)) && (empty$(school)) )
%   {
%     warning$("empty address and school in " * cite$());
%   } else
%   {
%     if (empty$(address))
%     {
%     } else
%     {
%       s = address;
%     }
%     if (empty$(location))
%     {
%     } else
%     {
%       s = location;
%     }
%     if (empty$(year))
%     {
%       warning$("empty year in " * cite$());
%     } else
%     {
%       if (empty$(s))
%       {
%         s = year;
%       } else
%       {
%         s = comma.connect(s, year);
%       }
%     }
%   }
%   return s;
% }
FUNCTION {format.thesis.info}
{ "" 's :=
  address empty$
  location empty$
  school empty$
  and
  and
    { "empty address and school in " cite$ * warning$ }
    { address empty$
        'skip$
        { address 's := }
      if$
      location empty$
        'skip$
        { location 's := }
      if$
      year empty$
        { "empty year in " cite$ * warning$ }
        { s empty$
            { year 's := }
            { s year comma.connect 's := }
          if$
        }
      if$
    }
  if$
  s
}

% Formatuje informace o skole v akademicke (diplomova, dizertacni atp.) praci.
%
% string format.thesis.school ()
% {
%   string s = "";
%   if (empty$(school))
%   {
%     warning$("empty school in " * cite$());
%   } else
%   {
%     if (empty$(s))
%     {
%       s = school;
%     } else
%     {
%       s = s * ". " * school;
%     }
%   }
%   return s;
% }
FUNCTION {format.thesis.school}
{ "" 's :=
  school empty$
    { "empty school in " cite$ * warning$  }
    { s empty$
        { school 's := }
        { s ". " * school * 's := }
      if$
    }
  if$
  s
}

% Formatting of number of pages and appendices in theses.
%
% string format.thesis.range ()
% {
%   if ( (empty$(format.range())) && (!empty$(inserts)) )
%   {
%     return format.range();
%   } else
%   {
%     if (is.ord(inserts))
%     {
%       return comma.connect(format.range(), inserts * " apps.");
%     } else
%     {
%       return comma.connect(format.range(), inserts);
%     }
%   }
% }
FUNCTION {format.thesis.range}
{ pages empty$
  { "" 's := }
  { pages opt.pages tie.or.connect 's := }
  if$
  s empty$
  { "" }
  { inserts empty$
    { s }
    { s inserts opt.pages tie.or.connect " apps." * comma.connect }
    if$
  }
  if$
}

% Thesis type formatting.
%
% string format.thesis.type (string basic.thesis.type)
% {
%   if (empty$(type))
%   {
%     return basic.thesis.type;
%   } else
%   {
%     return capitalize(type);
%   }
% }
FUNCTION {format.thesis.type}
{ type empty$
  'skip$
  { pop$ type capitalize }
  if$
}


% Formatting of type and number of technical report.
%
% string format.report.type ()
% {
%   if (empty$(type))
%   {
%     push("");
%   } else
%   {
%     push(type);
%   }
%   if (empty$(number))
%   {
%   } else
%   {
%     space.connect(pop(), number);
%   }
% }
FUNCTION {format.report.type}
{ type empty$
    { "" }
    { type }
  if$
  number empty$
    'skip$
    { number space.connect }
  if$
}


% Checks if all elements used for @MISC record are empty.
%
% int empty.misc.check ()
% {
%   if ( (empty$(author)) && (empty$(title)) && (empty$()) &&
%        (empty$(howpublished)) && (empty$(month)) && (empty$(year)) &&
%        (empty$(note)) && (!empty$(key)) )
%   {
%     warning$("all misc relevant fields are empty in " * cite$());
%     return 1;
%   } else
%   {
%     return 0;
%   }
% }
FUNCTION {empty.misc.check}
{ author empty$ title empty$ howpublished empty$
  month empty$ year empty$ note empty$
  and and and and and
  key empty$ not and
  { "all misc relevant fields are empty in " cite$ * warning$
    #1
  }
  { #0 }
  if$
}


% Formats place of publication and institution of a technical report.
%
% string format.report.details ()
% {
%   if ( empty$(institution) )
%   {
%     if ( empty$(address) && empty$(organization) )
%     {
%       return "";
%     } else
%     {
%       if (empty$(address))
%       {
%         push("");
%       } else
%       {
%         push(opt.sep.a() * " " * address * opt.sep.p());
%       }
%       if (empty$(organization))
%       {
%         warning$("empty organization in " * cite$());
%       } else
%       {
%         return pop() * organization;
%       }
%     }
%   } else
%   {
%     if ( empty$(address) && empty$(institution) )
%     {
%       return "";
%     } else
%     {
%       if (empty$(address))
%       {
%%         push("[b.m.]");
%         push("");
%       } else
%       {
%         push(opt.sep.a() * " " * address * opt.sep.p());
%       }
%       if (empty$(institution))
%       {
%         warning$("institution is empty in " * cite$());
%       } else
%       {
%         return pop() * institution;
%       }
%     }
%   }
% }
FUNCTION {format.report.details}
{ institution empty$
  {
    address empty$
    organization empty$
    and
      { "" }
      { address empty$
          { "" }
          { address }
        if$
        organization empty$
          { "empty organization in " cite$ * warning$ }
          { address empty$
              { organization * }
              { opt.sep.p * organization * }
            if$
          }
        if$
      }
    if$
  }
  {
    address empty$
    institution empty$
    and
      { "" }
      { address empty$
          { "" }
          { address }
        if$
        institution empty$
          { "empty institution in " cite$ * warning$ }
          { address empty$
              { institution * }
              { opt.sep.p * institution * }
            if$
          }
        if$
      }
    if$
  }
  if$
}


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%% CODE -- FUNCTIONS FOR RECORD PROCESSING %%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Processes @Article type records.
% Bibliographic citations of an article in a serial publication (magazine article).
%
% Required elements:   author, title, journal, edition, year, volume, number, pages, issn
% Optional elements: subtitle, journalsubtitle, publisher, address, contributory, url*, month, note, doi
% Elements of el. doc.:  howpublished*, revised*, cited*, version
%
% * Required element for el. doc.
FUNCTION {article}
{ output.bibitem
  format.authors
  duplicate$ empty$
  { pop$
    "empty author in " cite$ * warning$
  }
  { output.authors }
  if$
  new.block %% author (required)
  format.title "title" output.check
  new.block %% journal title (required)
  format.journal "journal" output.check
  format.howpublished output
  new.block
  format.edition "edition" output.check
  new.block %% edition (required)
  contributory capitalize output
  new.block %% contributory (optional)
  format.publish.info output
  new.block
  format.journal.issue
  format.pages comma.connect
  format.revised comma.connect
  format.cited comma.connect output
  new.block %% location in source document (required)
  format.cseries output
  new.block
  format.doi output
  new.block
  format.issn output %% standard number ISSN (optional)
  new.block
  note output
  new.block %% poznamky (volitelne)
  howpublished empty$
  { format.url output }
  { format.url "url" output.check
    year empty$
    revised empty$
    and
    { "empty year and revised in " cite$ * warning$ }
    'skip$
    if$
  }
  if$
  fin.entry 
}% @Article

% Processes @Book type record.
% Bibliographic citation of a monographic publication (book).
%
% Required elements:   author nebo editor, title, edition, address, publisher, year, isbn
% Optional elements: subtitle, contributory, month, pages, series, number nebo volume, note, url*
% Elements of el. doc.:  howpublished*, revised*, cited*, version
%
% * Required element for el. doc.
FUNCTION {book}
{ output.bibitem
  author.or.editor
  new.block %% author (required)
  format.btitle "book title" output.check
  format.howpublished output
  new.block %% title (required) and medium type (required for el. doc.)
  format.edition capitalize "edition" output.check
  new.block %% edition (required)
  contributory capitalize output
  new.block %% secondary author(s) (optional)
  format.publish.info
  format.date comma.connect
  format.revised comma.connect
  format.cited tie.or.connect
  capitalize output
  new.block %% publisher information (optional), date of publication (required),
            %% date of revision/update and citation (required for el. doc.)
  format.range output
  new.block %% pages (optional)
  format.cseries output
  new.block %% series (optional)
  format.isbn "isbn" output.check
  new.block
  note capitalize output
  new.block %% notes (optional)
  howpublished empty$
  { format.url output }
  { format.url "url" output.check
    year empty$
    revised empty$
    and
    { "empty year and revised in " cite$ * warning$ }
    'skip$
    if$
  }
  if$
  fin.entry %% standard number ISBN (required)
}% @Book

% @Booklet is merely a reference to @Book.
FUNCTION {booklet} { book }

% Processes @Conference and @InProceedings, InCollection and @InBook type records.
% Bibliographic citation of conference proceedings entry (paper).
%
% Required elements:   author, title, editor nebo organization, booktitle,
%       edition, address, publisher, year, pages, isbn or issn.
% Optional elements: subtitle, booksubtitle, contributory, month, series, editionnumber, chapter, number nebo volume, note, doi url*.
% Elements of el. doc.:  howpublished*, revised*, cited*, version.
%
% * Required element for el. doc.
FUNCTION {conference}
{ output.bibitem
  author.or.editor
  new.block %% author (required)
  format.title "title" output.check
  new.block %% title
  conference.basics
  format.howpublished tie.or.connect output
  new.block %% editor and title of proceedings
%  format.edition "edition" output.check
  format.edition output
  new.block %% edition
  contributory capitalize output
  new.block %% podrizena odpovednost (volitelna)
  format.publish.info
  format.date capitalize comma.connect
  format.cvolume comma.connect
  format.chapter comma.connect
  format.pages comma.connect
  format.revised comma.connect
  format.cited tie.or.connect
  capitalize output
  new.block %% publisher information, date of publication, revision/update and citation
  format.cseries output
  new.block %% series (if numbered, include number or volume)
  format.doi output
  new.block
  isbn empty$
    { issn empty$
        { "empty isbn and issn in " cite$ * warning$ }
        { format.issn output }
      if$
    }
    { format.isbn output }
  if$
  new.block %% standard numbers ISBN or ISSN (required)
  note capitalize output
  new.block %% notes
  howpublished empty$
    { format.url output }
    { format.url "url" output.check
      year empty$
      revised empty$
      and
        { "empty year and revised in " cite$ * warning$ }
        'skip$
      if$
    }
  if$
  fin.entry
} % @Conference and @InProceedings, @InCollection and @InBook

% @InProceedings is simply a reference to @Conference. 
FUNCTION {inproceedings} { conference }
FUNCTION {incollection} { conference }
FUNCTION {inbook} { conference }

% Processes @Proceedings type of records.
% 
% Bibliographic citation of monographies (books, proceedings and such).
%
% Required elements:   author or editor, title, edition, year, isbn or issn.
% Optional elements: subtitle, contributory, address, publisher, month, series, number or volume, note, doi, url*.
% Elements of el. doc.:  howpublished*, revised*, cited*, version.
%
% * Required element for el. doc.
FUNCTION {proceedings}
{ output.bibitem
  author.or.editor
  new.block %% primary author(s) (required)
  format.btitle
  format.howpublished tie.or.connect output
  new.block %% primary author and title of proceedings
%  format.edition "edition" output.check
  format.edition output
  new.block %% edition
  contributory capitalize output
  new.block %% secondary author(s) (optional)
  format.publish.info
  format.date capitalize comma.connect
  format.cvolume comma.connect
  format.chapter comma.connect
  format.pages comma.connect
  format.revised comma.connect
  format.cited tie.or.connect
  capitalize output
  new.block %% publishing information, date of publication, revision/update and citation
  format.cseries output
  new.block %% series (if numbered, then include it's number or volume)
  format.doi output
  new.block
  isbn empty$
    { issn empty$
        { "empty isbn and issn in " cite$ * warning$ }
        { format.issn output }
      if$
    }
    { format.isbn output }
  if$
  new.block %% ISBN or ISSN (required)
  note capitalize output
  new.block %% notes
  howpublished empty$
    { format.url output }
    { format.url "url" output.check
      year empty$
      revised empty$
      and
        { "empty year and revised in " cite$ * warning$ }
        'skip$
      if$
    }
  if$
  fin.entry
} % @Proceedings


% Processes @Misc, @Webpage and @Website type records.
%
% Required elements:   At least one of the optional elements!
% Optional elements: author, secondarytitle, title, subtitle, howpublished, contributory, edition, month, doi, issn
%       year, revised*, cited, note, url, version
%
% Note: Displays warning if the year element is not used. Should not call format.date
FUNCTION {misc}
{ empty.misc.check
  'skip$
  { output.bibitem
    format.authors
    duplicate$ empty$
    { pop$
    }
    { output.authors }
    if$
    new.block
    secondarytitle output
    new.block
    format.btitle "title" output.check
    format.howpublished output
    new.block
    contributory capitalize output
    new.block %% secondary author(s) (optional)
    format.edition output
    new.block
    format.publish.info
    edition empty$
    address empty$
    publisher empty$
    and
    and
      { 
        output
        day empty$
        { format.date capitalize output }
        { day ". " * format.date * output }
        if$
      }
      { 
        day empty$
        { format.date comma.connect output }
        { day ". " * format.date * comma.connect output }
        if$
      }
    if$
    time output
    revised empty$
      'skip$
      { 
        new.block
        format.revised output
      }
    if$
    format.cited makesmall output
    new.block
    format.doi output
    new.block
    format.issn output %% standardni cislo ISSN (volitelne)
    new.block
    note capitalize makesmall output
    new.block
    howpublished empty$
    { format.url output }
    { format.url "url" output.check
      year empty$
      revised empty$
      and
      { "empty year and revised in " cite$ * warning$ }
      'skip$
      if$
    }
    if$
    new.block
    format.path output
    fin.entry
  }
  if$
}%@Misc and @Webpage and @Website
% @Webpage and @Website are merely references to @Misc.
FUNCTION {webpage} { misc }
FUNCTION {website} { misc }


% Processes @Manual, @TechReport and @Unpublished type records..
%
% Required elements:  author, title, edition, address, organization or institution, month, year, revised
% Optional elements: note, number, url*
% Elements of el. doc.:  howpublished*, cited*
%
% Note: * Required element for el. doc.
FUNCTION {techreport}
{ output.bibitem
  author.or.editor
  new.block %% author (required)
  format.btitle "title" output.check
  format.howpublished output
  new.block
  format.report.type
  format.edition capitalize comma.connect output
  new.block
  contributory capitalize output
  new.block %% podrizena odpovednost (volitelna)
  format.report.details
  format.date comma.connect
  format.revised comma.connect
  format.cited tie.or.connect
  capitalize output
  new.block %% publishing institution information (optional) and date of publication (required)
  format.range output
  new.block %% number of pages (optional)
  note capitalize output
  new.block %% notes (optional)
  format.url output
  new.block %% availability (optional)
  fin.entry
}%@Manual, @TechReport and @Unpublished
FUNCTION {manual} { techreport }
FUNCTION {unpublished} { techreport }

% Processes @BachelorsThesis, @MastersThesis and @PhdThesis type records.
% Bibliographic citation of theses (bachelor's, master's and dissertation).
%
% Default version -- value #0 in choose.thesis.
%
% Required elements:   author, title, address or location, school, year, type
% Optional elements: subtitle, pages, inserts, note, url, isbn, howpublished
FUNCTION {thesis}
{ output.bibitem
  format.authors
  duplicate$ empty$
    { pop$
      "empty author in " cite$ * warning$
    }
    { output.authors }
  if$
  new.block %% author (required)
  format.btitle "title" output.check
  format.howpublished output
  new.block %% title (required)
  format.thesis.info capitalize output
  new.block %% school and year (required)
  format.cited makesmall output
  new.block %% cited
  format.thesis.range output
  new.block %% number of pages and appendices (optional)
  type$ "l" change.case$
  duplicate$ "bachelorsthesis" =
    { pop$ "Bachelor's thesis" }
    { "mastersthesis" =
        { "Master's thesis" }
        { "Dissertation" }
      if$
    }
  if$
  format.thesis.type output
  new.block %% thesis type (optional -- implicit type used)
  format.thesis.school output
  new.block
  format.isbn output
  new.block
  format.supervisor output
  note capitalize output
  new.block
  format.url output
  fin.entry %% standard number ISBN (optional)
}%@BachelorsThesis, @MastersThesis a @PhdThesis

% @BachelorsThesis type records are processed by thesis function.
FUNCTION {bachelorsthesis}{ thesis }

% @MastersThesis type records are processed by thesis function.
FUNCTION {mastersthesis} { thesis }

% @PhdThesis type records are processed by thesis function.
FUNCTION {phdthesis} { thesis }

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%% SLIGHTLY EDITED FUNCTIONS TAKEN FROM plain.bst %%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% loads bibliographic database
READ

% Prepares sorting value -- applies built-in function purify$ and converts 
% to small letters.
FUNCTION {sortify}
{ purify$
  "l" change.case$
}

% Declaration of another integer variable.
INTEGERS { len }

% Returns only a part of input value.
FUNCTION {chop.word}
{ 's :=
  'len :=
  s #1 len substring$ =
    { s len #1 + global.max$ substring$ }
    's
  if$
}

% Prepares names for sorting.
FUNCTION {sort.format.names}
{ 's :=
  #1 'nameptr :=
  ""
  s num.names$ 'numnames :=
  numnames 'namesleft :=
    { namesleft #0 > }
    { nameptr #1 >
  { "   " * }
  'skip$
      if$
%       s nameptr "{vv{ } }{ll{ }}{  ff{ }}{  jj{ }}" format.name$ 't :=
      s nameptr "{ll{ }}{  ff{ }}{  vv{ }}" format.name$ 't :=  %% Zamena!
      nameptr numnames = t "others" = and
  { opt.etal * }
  { t sortify * }
      if$
      nameptr #1 + 'nameptr :=
      namesleft #1 - 'namesleft :=
    }
  while$
}

% Prepares titles for sorting.
FUNCTION {sort.format.title}
{ 't :=
  "A " #2
    "An " #3
      "The " #4 t chop.word
    chop.word
  chop.word
  sortify
  #1 global.max$ substring$
}

% Sort by author.
FUNCTION {author.sort}
{ key empty$
    { author empty$
      editor empty$
      and
        { "to sort, need author or key in " cite$ * warning$
          ""
        }
        { author empty$
            { editor sort.format.names }
            { author sort.format.names }
          if$
        }
      if$
    }
    { key sortify }
  if$
}
% Dytrych: Původní funkce upřednostní jméno před klíčem a ten pak nelze použít k opravě českého řazení.
% FUNCTION {author.sort}
% { author empty$
%     { key empty$
%   { "to sort, need author or key in " cite$ * warning$
%     ""
%   }
%   { key sortify }
%       if$
%     }
%     { author sort.format.names }
%   if$
% }

% Sort by editor.
FUNCTION {author.editor.sort}
{ key empty$
    { author empty$
  { editor empty$
      { "to sort, need author, editor, or key in " cite$ * warning$
        ""
      }
      { editor sort.format.names }
    if$
  }
  { author sort.format.names }
      if$
    }
    { key sortify }
  if$
}
% FUNCTION {author.editor.sort}
% { author empty$
%     { editor empty$
%   { key empty$
%       { "to sort, need author, editor, or key in " cite$ * warning$
%         ""
%       }
%       { key sortify }
%     if$
%   }
%   { editor sort.format.names }
%       if$
%     }
%     { author sort.format.names }
%   if$
% }

% Sort by author, editor or organization. Unused function.
FUNCTION {author.organization.sort}
{ author empty$
    { organization empty$
  { key empty$
      { "to sort, need author, organization, or key in " cite$ * warning$
        ""
      }
      { key sortify }
    if$
  }
  { "The " #4 organization chop.word sortify }
      if$
    }
    { author sort.format.names }
  if$
}

% Sort by editor or organization. Unused function.
FUNCTION {editor.organization.sort}
{ editor empty$
    { organization empty$
  { key empty$
      { "to sort, need editor, organization, or key in " cite$ * warning$
        ""
      }
      { key sortify }
    if$
  }
  { "The " #4 organization chop.word sortify }
      if$
    }
    { editor sort.format.names }
  if$
}

% Prepare for sorting.
%
% Function is edited version of the original function in plain.bst.
FUNCTION {presort}
{ type$ "book" =
  type$ "inbook" =
  or
  { author.editor.sort }
  { author.sort }
  if$
  "    " *
  year field.or.null sortify *
  "    " *
  title field.or.null
  sort.format.title *
  #1 entry.max$ substring$
  'sort.key$ :=
}

% Executes preparation before sort.
ITERATE {presort}

% Executes sort.
SORT

% Declaration of string variable used to identify the longest label for bib.cit.
STRINGS { longest.label }

% Declaration of auxiliary numerical variables.
INTEGERS { number.label longest.label.width }

% Initialization of auxiliary variables.
FUNCTION {initialize.longest.label}
{ "" 'longest.label :=
  #1 'number.label :=
  #0 'longest.label.width :=
}

% Passes longest label.
FUNCTION {longest.label.pass}
{ number.label int.to.str$ 'label :=
  number.label #1 + 'number.label :=
  label width$ longest.label.width >
    { label 'longest.label :=
      label width$ 'longest.label.width :=
    }
    'skip$
  if$
}

% Executes initialization of auxiliary variables.
EXECUTE {initialize.longest.label}

% Selects longest label.
ITERATE {longest.label.pass}

% This function handles first rows, that appear in output file.
%
% This function is extended with a conditional definition of \url{} command.
FUNCTION {begin.bib}
{ "\makeatletter" write$ newline$
%%%  "\@ifundefined{url}"
%%%    "{\def\url#1{{\tt $<$#1$>$}}}" *
%%%    "{\DeclareUrlCommand\url{\def\UrlLeft{<} \def\UrlRight{>} \urlstyle{tt}}}" *
%%%    write$ newline$
  "\makeatother" write$ newline$
  preamble$ empty$
    'skip$
    { preamble$ write$ newline$ }
  if$
  "\begin{thebibliography}{"  longest.label  * "}" * write$ newline$
}

% Functions writes last lines to output file -- closes environment thebibliography.
FUNCTION {end.bib}
{ newline$
  "\end{thebibliography}" write$ newline$
}

% Inserts the beginning of thebibliography environment to the output file.
EXECUTE {begin.bib}

% Executes initialization of necessary constants.
EXECUTE {init.state.consts}

% Processes all cited records.
ITERATE {call.type$}

% Closes thebibliography environment.
EXECUTE {end.bib}
