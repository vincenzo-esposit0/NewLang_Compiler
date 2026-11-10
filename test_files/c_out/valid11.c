#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
char* classify(int);
void program();
char* intToString(int var){
    char* int_str = malloc(256);
    sprintf(int_str, "%d", var);
    return int_str;
}

char* doubleToString(double var){
    char* double_str = malloc(256);
    sprintf(double_str, "%f", var);
    return double_str;
}

char* boolToString(int var){
    if (var == 1){
        return "true";
    }
    if (var == 0){
        return "false";
    }
    return "";
}

char* concat(char* s1, char* i) {
    char* s = malloc(256);
    sprintf(s, "%s%s", s1, i);
    return s;
}
char* classify(int n){
char* label = "";
label = "small";
if(n >= 10){
	label = "large";
}
return label;
}
void program(){
float x = 2.5;
int a = 10;
int b = 9;
	printf("%s",classify(a));
	printf("%s"," ");
	printf("%s",classify(b));
	printf("\n");
	printf("%s",boolToString(a >= b));
	printf("%s"," ");
	printf("%s",boolToString(b >= a));
	printf("%s"," ");
	printf("%s",boolToString(a >= a));
	printf("\n");
	printf("%s",boolToString(x >= 2.5));
	printf("%s"," ");
	printf("%s",boolToString(x >= a));
	printf("\n");
}
int main(int argc, char** argv){ 
program();
return (EXIT_SUCCESS);
}
