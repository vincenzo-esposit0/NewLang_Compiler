#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
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
void program(){
float sum;
int i = 1;
float r = 2.2;
char* s = "text";
int ok = 1;
char c = 'z';
sum = i + r;
	printf("%d",i);
	printf("%s"," ");
	printf("%f",r);
	printf("%s"," ");
	printf("%s",s);
	printf("%s"," ");
	printf("%s",boolToString(ok));
	printf("%s"," ");
	printf("%c",c);
	printf("\n");
	printf("%f",sum);
	printf("\n");
}
int main(int argc, char** argv){ 
program();
return (EXIT_SUCCESS);
}
