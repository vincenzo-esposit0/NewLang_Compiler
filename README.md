# <p align="center"> Compiler - NewLang </p>

L'obiettivo di questo progetto è la creazione di un compilatore per il linguaggio NewLang. Sviluppato come parte dell'esame di Compilatori del corso di Laurea Magistrale in Software Engineering and IT Management, il compilatore si occupa della traduzione di programmi scritti in NewLang in programmi scritti nel linguaggio C.


## Scelte progettuali 

Il compilatore qui presente è stato sviluppato in conformità alle specifiche fornite; tutte le situazioni non chiaramente definite in tali specifiche sono state affrontate e gestite di conseguenza.

### Visite degli alberi
L'elaborazione delle diverse fasi della compilazione, tra cui la *costruzione dell'albero sintattico*, la *gestione dello scoping*, il *type checking* e la *generazione del codice intermedio C*, è affidata a quattro classi separate che implementano il pattern Visitor. Questa implementazione consente di visitare tutti i nodi dell'albero in modo efficiente.

La suddivisione delle fasi distintive in classi separate ha favorito la creazione di un codice più manutenibile e flessibile.

#### 1. MySyntaxTree
Ha il compito di creare l'albero sintattico e di fornire una sua rappresentazione in formato XML.

#### 2. MyScopingVisitor
Si occupa di gestire lo scoping del linguaggio, grazie allo scoping visitor è stato possibile gestire l'uso prima della dichiarazione sia di una variabile che di una funzione.
I costrutti che permettono la creazione di un nuovo scope in NewLang sono:
  - while;
  - for;
  - if; 
  - else;
  - dichiarazione di funzione;
  - intero programma;
  - tutti i costrutti che hanno al loro interno un body delegano a quest'ultimo la creazione e la gestione dello scope.
  
  Inoltre viene anche data la possibilità di avere in scope innestati dichiarazioni di variabili già esistenti in scope superiori (Most-closely-nested rule).


#### 3. MyTypeVisitor
Questo visitor ha la responsabilità di verificare i tipi in conformità alle specifiche del sistema di tipi del linguaggio, nonché di individuare eventuali errori semantici. All'interno di questa classe, viene realizzata l'inferenza di tipo.

#### 4. MyCTranslatorVisitor
Il compito di questo visitor è effettuare la traduzione del codice scritto in NewLang in codice C, il quale successivamente sarà compilato mediante GCC.

### Tipi e conversioni

Il linguaggio NewLang presenta i seguenti tipi: *real*, *integer*, *char*, *string* e *bool*. Nella proposta linguistica, non sono previsti casting impliciti per nessun tipo, ad eccezione del tipo integer. In pratica, qualsiasi espressione di tipo integer può essere assegnata o passata a una variabile di tipo real (vale la pena notare che la situazione opposta non è vera).
 
 ##### Inferenza di tipo:
Il linguaggio NewLang offre la facoltà di dichiarare una variabile utilizzando il tipo "var" e contemporaneamente assegnarle un valore costante (es. var x << 10). Nell'analisi semantica di questo contesto, è necessario dedurre il tipo intero di x dal tipo della costante 10 e aggiungere l'associazione (x, int) all'ambiente dei tipi corrente.

### Funzioni Write, WriteLn e Read
Queste funzioni consentono di effettuare operazioni di lettura e scrittura tramite standard output:

- **Write:()-->:**
  - Consente la comunicazione con l'utente attraverso la console. La funzione accetta almeno un parametro, o più parametri intervallati da ",".

- **Writeln:()-->!;**
  - Simile alla precedente, ma dopo la scrittura va a capo.

- **Read:**
  - **<variabile><--;**
    - Legge un input dalla console e lo assegna alla variabile.
  - **<variabile><-- <"STRINGA">;**
    - Presenta due versioni: una in cui preleva un input dalla console e lo assegna a una variabile, e l'altra in cui viene aggiunta una stringa che funge da label.
### Vincoli 
I seguenti vincoli sono stati introdotti per potenziare l'usabilità e la chiarezza:
 - Non è consentito dichiarare una funzione con il nome "main", né con il nome "start:"
  
## Get Started
Inizialmente, è necessario:
  - Effettuare il clone del repository indicato.
  - Avviare la compilazione mediante Maven.
  

Il software riceve in ingresso un file .txt contenente codice scritto nel linguaggio NewLang e successivamente genera un file .c, il quale viene utilizzato per creare l'eseguibile.


```diff
- E' essenziale avere GCC installato sulla macchina!
- E' indispensabile che Maven sia presente sull'ambiente di sviluppo!
- Assicurarsi che Java sia installato sulla macchina è fondamentale!

```
### Esempio di programma scritto in NewLang
```c
|*
 Programma esemplificativo del linguaggio NewLang
|


def sommac(integer a, d | real b | out string size): real
{
	real result;


	result  <<  a + b + c + d;

	if result > 100 then{
		var valore << "grande";
 		size << valore; }
	else {
		var valore << "piccola";
 		size << valore;
	}

	return result;
}


var c << 1;


start:
def esercizio() : void {  || funzione principale


    var a << 1, b << 2.2, x << 3;

    string taglia, ans1;
    var ans << "no";
    real risultato << sommac(a, x, b, taglia);


    stampa("la somma  incrementata  è " );
    (taglia) -->!;
     stampa(" ed è pari a " );
     (risultato) -->!;

	("vuoi continuare? (si/no) - inserisci due volte la risposta")  -->! ;
	ans, ans1 <--;
    while ans = "si" loop {

        a <-- "inserisci un intero:";
        b <-- "inserisci un reale:";
        risultato << sommac(a, x, b, taglia);
        stampa("la somma  incrementata  è " );
        (taglia) -->!;
        stampa(" ed è pari a " );
        (risultato) -->!;
        ans <-- "vuoi continuare? (si/no):";
    }

    ("") -->! ;
    ("ciao") -->;
}


def stampa(string messaggio): void {  || funzione di stampa

    integer a;
    integer i;
	for x << 4 to 1 loop {
		("") -->! ;
	}

	(messaggio) -->! ;

}

```
## La sua traduzione in C

```c
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>

float sommac (int, int, float, char **);
void esercizio ();
void stampa (char *);

char * intToString (int var)
{
  char *int_str = malloc (256);
  sprintf (int_str, "%d", var);
  return int_str;
}

char * doubleToString (double var)
{
  char *double_str = malloc (256);
  sprintf (double_str, "%f", var);
  return double_str;
}

char * boolToString (int var)
{
  if (var == 1)
    {
      return "true";
    }
  if (var == 0)
    {
      return "false";
    }
  return "";
}

char * concat (char *s1, char *i)
{
  char *s = malloc (256);
  sprintf (s, "%s%s", s1, i);
  return s;
}

int c = 1;

float sommac (int a, int d, float b, char **size)
{
  float result;
  result = a + b + c + d;
  if (result > 100)
    {
      char *valore = "grande";
      *size = valore;
    }
  else
    {
      char *valore = "piccola";
      *size = valore;
    }
  return result;
}

void esercizio ()
{
  char *taglia = "";
  char *ans1 = "";
  int a = 1;
  int b = 2.2;
  int x = 3;
  char *ans = "no";

  float risultato = sommac (a, x, b, &taglia);

  stampa ("la somma  incrementata  C( ");

  printf ("%s\n", taglia);
  stampa (" ed C( pari a ");
  printf ("%f\n", risultato);
  printf ("%s\n",
	  "vuoi continuare? (si/no) - inserisci due volte la risposta");
  ans = malloc (256);
  scanf ("%s", ans);
  ans1 = malloc (256);
  scanf ("%s", ans1);

  while (!strcmp (ans, "si"))
    {
      printf ("inserisci un intero:\n");
      scanf ("%d", &a);
      printf ("inserisci un reale:\n");
      scanf ("%d", &b);
      risultato = sommac (a, x, b, &taglia);
      stampa ("la somma  incrementata  C( ");
      printf ("%s\n", taglia);
      stampa (" ed C( pari a ");
      printf ("%f\n", risultato);
      printf ("vuoi continuare? (si/no):\n");
      ans = malloc (256);
      scanf ("%s", ans);
    }
  printf ("%s\n", "");
  printf ("%s\n", "ciao");
}

int main (int argc, char **argv)
{
  esercizio ();
  return (EXIT_SUCCESS);
}

void stampa (char *messaggio)
{
  int a;
  int i;
  int x;
  for (x = 4; x >= 1; x--)
    {
      printf ("%s\n", "");
    }
  printf ("%s\n", messaggio);
}


```





  
      
    
 
