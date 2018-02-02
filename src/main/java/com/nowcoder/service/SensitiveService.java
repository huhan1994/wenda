package com.nowcoder.service;

import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;


import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class SensitiveService implements InitializingBean{
    private static final Logger logger = LoggerFactory.getLogger(SensitiveService.class);

    //增加关键词
    private void addWord(String lineText){
        TrieNode tempNode = rootNode;
        for(int i=0;i<lineText.length();i++){
            Character character = lineText.charAt(i);
            //空格等无效字符
            if(issymbol(character)){
                continue;
            }
            TrieNode node = tempNode.getSubNode(character);
            if(node==null){
                node=new TrieNode();
                tempNode.addNode(character,node);
            }
            tempNode = node;
            if(i==lineText.length()-1){
                tempNode.setKeywordEnd(true);
            }
        }
    }

    private TrieNode rootNode = new TrieNode();

    private boolean issymbol(char c){
        int ic=(int)c;
        //东亚文字 0x2E80-0x9FFF
        return !CharUtils.isAsciiAlphanumeric(c) &&(ic<0x2E80 || ic>0x9FFF);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("src/main/resources/SensitiveWords.txt"));
            String tempString=null;
            while((tempString=bufferedReader.readLine())!=null){
                addWord(tempString.trim());
            }
            bufferedReader.close();

//            InputStream is=Thread.currentThread().getContextClassLoader().getResourceAsStream("SensitiveWords.txt");
//            InputStreamReader read = new InputStreamReader(is);
//            BufferedReader bufferedReader = new BufferedReader(read);
//            String lineTxt;
//            while((lineTxt=bufferedReader.readLine())!=null){
//                addWord(lineTxt.trim());
//            }
//            read.close();
        }catch (Exception e){
            logger.error("读取敏感词失败"+e.getMessage());
        }
    }

    public String filter(String text){
        if(StringUtils.isBlank(text)){
            return text;
        }
        StringBuilder result = new StringBuilder();
        String replacement="***";

        TrieNode tempNode = rootNode;
        int begin = 0;
        int position = 0;

        while(position<text.length()){
            char c= text.charAt(position);

            if(issymbol(c)){
                if(tempNode==rootNode){
                    result.append(c);
                    begin++;
                }
                position++;
                continue;
            }

            tempNode=tempNode.getSubNode(c);
            if(tempNode==null){
                result.append(text.charAt(begin));
                position=begin+1;
                begin=position;
                tempNode=rootNode;
            }else if(tempNode.isKeyWord()){
                //发现敏感词
                result.append(replacement);
                position=position+1;
                begin=position;
                tempNode=rootNode;
            }else {
                position++;
            }
        }
        //最后一串
        result.append(text.substring(begin));
        return result.toString();
    }

    private class TrieNode{
        //判断是否为敏感词
        private boolean end;

        private Map<Character,TrieNode> subNodes=new HashMap<Character, TrieNode>();

        public void addNode(Character c,TrieNode node){
            subNodes.put(c,node);
        }

        TrieNode getSubNode(Character key){
            return subNodes.get(key);
        }

        boolean isKeyWord(){
            return end;
        }

        void setKeywordEnd(boolean end){
            this.end=end;
        }
    }

}
