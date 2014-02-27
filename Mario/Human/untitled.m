file = fopen('allEmotions.txt');
A=fscanf(file,'%f %f',[7 inf]);
A=A';
plot(A);
legend('neutral','happy','surprised','angry','disgusted','afraid','sad');