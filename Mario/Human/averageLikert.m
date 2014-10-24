for j=0:4 %for all chunks
    
    
    name = strcat(strcat('model',num2str(j)),'.arff');

    M = csvread(name,17,0);

    X = M(:,1); %difficulties
    Y = M(:,12); %likert

    A = [0,0,0,0,0]; %total likert
    B = [0,0,0,0,0]; %number of instances

        for i=1:size(M,1) %for every instance
            q = X(i);
            A(q) = A(q)+Y(i); 
            B(q) = B(q)+1;
        end

    AVG(:,j+1) = A(:)./B(:); %what we need

end
z = [1,2,3,4,5];
figure();
plot(AVG);
legend('STRAIGHT','HILLS','TUBES','JUMP','CANNONS','Location','northwest');
set(gca,'xticklabel',{'1','','2','','3','','4','','5'});
xlabel('Difficulty level');
ylabel('Average Likert value');
saveTightFigure('averageLikertALL.png');