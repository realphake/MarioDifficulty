file = fopen('difficulties.txt');
A=fscanf(file,'%f %f',[5 inf]);
A=A';
A=A(:,:)


figure()
plot(A);
legend('STRAIGHT','HILLS','TUBES','JUMPS','CANNONS','Location','northwest');
xlabel('No. of measurements');
ylabel('Difficulty levels');
saveTightFigure('difficulties.png')

% file = fopen('allEmotions.txt');
% b=fscanf(file,'%f %f',[7 inf]);
% b=b';
% 
% figure()
% plot(b);
% legend('neutral','happy','surprised','angry','disgusted','afraid','sad');
% xlabel('No. of frames recorded');
% ylabel('Emotion probability estimates');
