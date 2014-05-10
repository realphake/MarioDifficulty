listing = dir('playerMeasurements');
innerDIR = cellstr('asd');
for i=1:13
   if ~strcmp(listing(i).name,'.')&&~strcmp(listing(i).name,'..')&&~strcmp(listing(i).name,'.DS_Store')
       innerDIR{i} = strcat('playerMeasurements/',listing(i).name);
       inner{i,1} = dir(innerDIR{i});
   end
end
innerDIR = innerDIR(4:13);
inner = inner(4:13);

easyEmotions = [];
normalEmotions =[];
hardEmotions = [];
easyDiffs = [];
normalDiffs = [];
hardDiffs = [];


% inner2DIReasy = cellstr('foo');
% inner2DIRnormal = cellstr('foo');
% inner2DIRhard = cellstr('foo');


for j= 1: size(inner,1)
    inner2DIReasy = strcat(innerDIR{j},'/easy');
    inner2DIRnormal = strcat(innerDIR{j},'/normal');
    inner2DIRhard = strcat(innerDIR{j},'/hard');
    
    easydiffs = strcat(inner2DIReasy,'/difficulties.txt');
    normaldiffs = strcat(inner2DIRnormal,'/difficulties.txt');
    harddiffs = strcat(inner2DIRhard,'/difficulties.txt');
    
    easyemotions = strcat(inner2DIReasy,'/allEmotions.txt');
    normalemotions = strcat(inner2DIRnormal,'/allEmotions.txt');    
    hardemotions = strcat(inner2DIRhard,'/allEmotions.txt');
    
    
    if exist(easydiffs,'file')
        file = fopen(easydiffs);
        A=fscanf(file,'%f %f',[5 inf]);
        A=A';
        easyDiffs = [easyDiffs;A];
    end
    
    if exist(normaldiffs,'file')
        file2 = fopen(normaldiffs);
        B=fscanf(file2,'%f %f',[5 inf]);
        B=B';
        normalDiffs = [normalDiffs;B];
    end

    if exist(harddiffs,'file')
        file3 = fopen(harddiffs);
        C=fscanf(file3,'%f %f',[5 inf]);
        C=C';
        hardDiffs = [hardDiffs;C];
    end
    
    if exist(easyemotions,'file')
        file = fopen(easyemotions);
        A=fscanf(file,'%f %f',[7 inf]);
        A=A';
        easyEmotions = [easyEmotions;A];
    end
    
    if exist(normalemotions,'file')
        file2 = fopen(normalemotions);
        B=fscanf(file2,'%f %f',[7 inf]);
        B=B';
        normalEmotions = [normalEmotions;B];
    end
    
    if exist(hardemotions,'file')
        file3 = fopen(hardemotions);
        C=fscanf(file3,'%f %f',[7 inf]);
        C=C';
        hardEmotions = [hardEmotions;A];
    end
end

 
% figure()
% plot(easyDiffs);
% legend('STRAIGHT','HILLS','TUBES','JUMPS','CANNONS','Location','northwest');


% figure()
% plot(easyEmotions);
% legend('neutral','happy','surprised','angry','disgusted','afraid','sad');


for q=1:5
    easyDiffsMean(q) = mean(easyDiffs(:,q));
    normalDiffsMean(q) = mean(normalDiffs(:,q));
    hardDiffsMean(q) = mean(hardDiffs(:,q));
end

for z=1:7
    easyEmotionsMean(z) = mean(easyEmotions(:,z));
    normalEmotionsMean(z) = mean(normalEmotions(:,z));
    hardEmotionsMean(z) = mean(hardEmotions(:,z));
end
