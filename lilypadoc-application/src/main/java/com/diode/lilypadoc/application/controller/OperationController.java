package com.diode.lilypadoc.application.controller;

import com.diode.lilypadoc.application.core.SpringLilypadoc;
import com.diode.lilypadoc.application.service.GitService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("/operation")
@RequestMapping("/operation")
public class OperationController {

    @Resource
    private SpringLilypadoc springLilypadoc;

    @Resource
    private GitService gitService;

    @PostMapping(value = "/parseAll")
    public void initAll(){
        log.info("同步git结果:{}", gitService.cloneOrPullApiRepo());
        log.info("删除原始目录结果:{}", springLilypadoc.deleteAll());
        log.info("同步结果:{}", springLilypadoc.parseAll());
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping(value = "getData")
    public String get(){
        return """
                {
                  "columns": [
                    {
                      "title": "Name",
                      "dataIndex": "name",
                      "key": "name"
                    },
                    {
                      "title": "Age",
                      "dataIndex": "age",
                      "key": "age",
                      "width": "12%"
                    },
                    {
                      "title": "Address",
                      "dataIndex": "address",
                      "width": "30%",
                      "key": "address"
                    }
                  ],
                  "data": [
                    {
                      "key": 1,
                      "name": "John Brown sr.",
                      "age": 60,
                      "address": "New York No. 1 Lake Park",
                      "children": [
                        {
                          "key": 11,
                          "name": "John Brown",
                          "age": 42,
                          "address": "New York No. 2 Lake Park"
                        },
                        {
                          "key": 12,
                          "name": "John Brown jr.",
                          "age": 30,
                          "address": "New York No. 3 Lake Park",
                          "children": [
                            {
                              "key": 121,
                              "name": "Jimmy Brown",
                              "age": 16,
                              "address": "New York No. 3 Lake Park"
                            }
                          ]
                        },
                        {
                          "key": 13,
                          "name": "Jim Green sr.",
                          "age": 72,
                          "address": "London No. 1 Lake Park",
                          "children": [
                            {
                              "key": 131,
                              "name": "Jim Green",
                              "age": 42,
                              "address": "London No. 2 Lake Park",
                              "children": [
                                {
                                  "key": 1311,
                                  "name": "Jim Green jr.",
                                  "age": 25,
                                  "address": "London No. 3 Lake Park"
                                },
                                {
                                  "key": 1312,
                                  "name": "Jimmy Green sr.",
                                  "age": 18,
                                  "address": "London No. 4 Lake Park"
                                }
                              ]
                            }
                          ]
                        }
                      ]
                    },
                    {
                      "key": 2,
                      "name": "Joe Black",
                      "age": 32,
                      "address": "Sydney No. 1 Lake Park"
                    }
                  ]
                }
                """;
    }
}