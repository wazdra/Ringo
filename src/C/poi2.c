#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <errno.h>

#include <sys/socket.h>
#include <sys/types.h>
#include <netdb.h>
#include <netinet/in.h>
#include <netinet/udp.h>
#include <pthread.h>
#include "util1.h"

void *receive_udp(void *couple){
  int sock = ((struct sock_addr_couple *)couple)->sockfd;
  struct sockaddr_in adsock = ((struct sock_addr_couple *)couple)->adsock;
  char *buff = malloc(100*sizeof(char));
  while(1){
    int rec = recv(sock,buff,100,0);
    buff[rec] = '\0';
    printf("Received: %sFrom port: %d\n",buff, adsock.sin_port);
  }
}

int main(int argc, char *argv[]) {
  if (argc != 3) {
    printf("Passer deux ports en argument : le port d'ecoute TCP puis le port d'ecoute UDP.\n");
    return -1;
  }

  struct sockaddr_in *addr_suiv;
  // addr_suiv->sin_family = AF_INET;
  int *port_suiv;
  struct sockaddr_in *addr_diff;
  int *port_diff;
  // PARTIE TCP (demande d'insertion)

  // demande d'insertion dans un anneau
  int sock1 = socket(PF_INET, SOCK_STREAM, 0);
  struct sockaddr_in adsock1;
  adsock1.sin_family = AF_INET;
  adsock1.sin_port = htons(atoi("4242" /* argv[3] */));
  inet_aton("192.168.43.212", &adsock1.sin_addr);
  int test = connect(sock1, (struct sockaddr *)&adsock1, sizeof(struct sockaddr));
  if(test) {
    printf("Erreur lors du premier connect TCP.\n");
    return -1;
  }

  printf("Premiere connection TCP reussie.\n");
  char *buff = malloc(100*sizeof(char));
  int rec = recv(sock1, (void *)buff, 100, 0);
  buff[rec]='\0';
  printf("%s\n", buff);
  char *welc_recu = malloc(5*sizeof(char));
  char *ip_recu = malloc(16*sizeof(char));
  char *port_recu = malloc(6*sizeof(char));
  char *ipdiff_recu = malloc(16*sizeof(char));
  char *portdiff_recu = malloc(6*sizeof(char));
  char *delim = malloc(2*sizeof(char));
  strcpy(delim, " ");
  strcpy(welc_recu, strtok(buff, delim));

  strcpy(ip_recu, strtok(NULL, delim));
  inet_aton(ip_recu, addr_suiv->sin_addr);

  strcpy(port_recu, strtok(NULL, delim));
  *port_suiv = atoi(port_recu);
  addr_suiv->sin_port = htons(*port_suiv);

  strcpy(ipdiff_recu, strtok(NULL, delim));
  inet_aton(ipdiff_recu, addr_diff->sin_addr);

  strcpy(portdiff_recu, strtok(NULL, delim));
  portdiff_recu[strlen(portdiff_recu)-1] = '\0';
  *port_diff = atoi(portdiff_recu);
  addr_suiv->sin_port = htons(*port_diff);

  // A ce stade les infos de connection UDP sont initialisees.
  free(buff);
  buff = malloc(sizeof(char)*100);

  strcpy(buff, "NEWC ");
  strcat(buff, "192.168.43.212");
  strcat(buff, " ");
  strcat(buff, argv[2]);
  strcat(buff, "\n");
    
  send(sock1, buff, sizeof(char)*100, 0);

  printf("%s", buff);
  
  int pid = fork();
  if (pid==0) { // fils

    // insertion d'un etranger dans l'anneau

    int sock2 = socket(PF_INET, SOCK_STREAM, 0);
    struct sockaddr_in adsock2;
    adsock2.sin_family = AF_INET;
    adsock2.sin_port = htons(atoi(argv[1]));
    bind(sock2, (struct sockaddr *)&adsock2, sizeof(struct sockaddr));
    listen(sock2, 16);
    socklen_t *rec_len;
    struct sockaddr rec_adsock;

    accept(sock2, (struct sockaddr *)&rec_adsock, rec_len);
    buff = malloc(100*sizeof(char));
    strcpy(buff, "WELC "); // "255.255.255.255 0\n");
    char *str_addr;
    // getnameinfo((struct sockaddr *)addr_suiv, sizeof(struct sockaddr), str_addr, sizeof(char)*16);
    strcat(buff, str_addr);
    strcat(buff, " ");
    char str_port[6];
    sprintf(str_port, "%d", *port_suiv);
    strcat(buff, str_port);
    strcat(buff, " ");

    char *str_diff;
    // getnameinfo((struct sockaddr *)addr_diff, sizeof(struct sockaddr), str_diff, sizeof(char)*16);
    strcat(buff, str_diff);
    strcat(buff, " ");
    sprintf(str_port, "%d", *port_diff);
    strcat(buff, str_diff);

    strcat(buff, "\n");
    send(sock2, buff, strlen(buff), 0);
    
    rec = recv(sock2, (void *)buff, 100, 0);
    buff[rec]='\0';
    char *newc_recu = malloc(5*sizeof(char));
    ip_recu = malloc(16*sizeof(char));
    port_recu = malloc(5*sizeof(char));
    delim = malloc(2*sizeof(char));
    strcpy(delim, " ");
    strcpy(newc_recu, strtok(buff, delim));
    strcpy(ip_recu, strtok(NULL, delim));
    strcpy(port_recu, strtok(NULL, delim));
    port_recu[strlen(port_recu)-1] = '\0';

    // changement de suivant
    addr_suiv->sin_addr.s_addr = htons(atoi(ip_recu));
    *port_suiv = atoi(port_recu);

    strcpy(buff, "ACKC\n");
    send(sock2, buff, strlen(buff), 0);

    return 0;
  }

  // PARTIE UDP (pere)

  int sock = socket(PF_INET,SOCK_DGRAM,0);
  
  addr_suiv->sin_addr.s_addr = htonl(INADDR_ANY);
  
  struct sock_addr_couple couple;
  couple.sockfd = sock;
  couple.adsock = *addr_suiv;


  int test2 = bind(sock,(struct sockaddr *)&addr_suiv,sizeof(struct sockaddr_in));
  if(test2 == 0){

    pthread_t thr;
    pthread_create(&thr,NULL,receive_udp,(void *)&couple);

    pthread_join(thr,NULL);

  }


  return 0;
}
