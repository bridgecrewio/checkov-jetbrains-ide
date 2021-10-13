package com.bridgecrew.ui

import javax.swing.*;
import com.intellij.json.psi.JsonObject
import com.intellij.openapi.ui.SimpleToolWindowPanel

class CheckovToolWindow : SimpleToolWindowPanel(false, true) {
//    val results = "{\n" +
//            "                \"check_id\": \"CKV_AWS_130\",\n" +
//            "                \"bc_check_id\": \"BC_AWS_NETWORKING_53\",\n" +
//            "                \"check_name\": \"Ensure VPC subnets do not assign public IP by default\",\n" +
//            "                \"check_result\": {\n" +
//            "                    \"result\": \"FAILED\",\n" +
//            "                    \"evaluated_keys\": [\n" +
//            "                        \"map_public_ip_on_launch\"\n" +
//            "                    ]\n" +
//            "                },\n" +
//            "                \"code_block\": [\n" +
//            "                    [\n" +
//            "                        61,\n" +
//            "                        \"resource aws_subnet \\\"eks_subnet1\\\" {\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        62,\n" +
//            "                        \"  vpc_id                  = aws_vpc.eks_vpc.id\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        63,\n" +
//            "                        \"  cidr_block              = \\\"10.10.10.0/24\\\"\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        64,\n" +
//            "                        \"  availability_zone       = \\\"${var .region}a\\\"\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        65,\n" +
//            "                        \"  map_public_ip_on_launch = true\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        66,\n" +
//            "                        \"  tags = merge({\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        67,\n" +
//            "                        \"    Name                                            = \\\"${local.resource_prefix.value}-eks-subnet\\\"\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        68,\n" +
//            "                        \"    \\\"kubernetes.io/cluster/${local.eks_name.value}\\\" = \\\"shared\\\"\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        69,\n" +
//            "                        \"    }, {\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        70,\n" +
//            "                        \"    git_commit                                       = \\\"6e62522d2ab8f63740e53752b84a6e99cd65696a\\\"\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        71,\n" +
//            "                        \"    git_file                                         = \\\"terraform/aws/eks.tf\\\"\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        72,\n" +
//            "                        \"    git_last_modified_at                             = \\\"2021-05-02 11:16:31\\\"\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        73,\n" +
//            "                        \"    git_last_modified_by                             = \\\"nimrodkor@gmail.com\\\"\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        74,\n" +
//            "                        \"    git_modifiers                                    = \\\"nimrodkor\\\"\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        75,\n" +
//            "                        \"    git_org                                          = \\\"bridgecrewio\\\"\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        76,\n" +
//            "                        \"    git_repo                                         = \\\"terragoat\\\"\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        77,\n" +
//            "                        \"    \\\"kubernetes.io/cluster/\$${local.eks_name.value}\\\" = \\\"shared\\\"\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        78,\n" +
//            "                        \"    yor_trace                                        = \\\"1fb4fa23-a5d6-4d6a-b7dc-88749383f48d\\\"\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        79,\n" +
//            "                        \"    }, {\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        80,\n" +
//            "                        \"    \\\"kubernetes.io/cluster/\$\$${local.eks_name.value}\\\" = \\\"shared\\\"\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        81,\n" +
//            "                        \"    \\\"kubernetes.io/cluster/\$${local.eks_name.value}\\\"  = \\\"shared\\\"\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        82,\n" +
//            "                        \"    }, {\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        83,\n" +
//            "                        \"    \\\"kubernetes.io/cluster/\$\$\$${local.eks_name.value}\\\" = \\\"shared\\\"\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        84,\n" +
//            "                        \"    \\\"kubernetes.io/cluster/\$\$${local.eks_name.value}\\\"  = \\\"shared\\\"\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        85,\n" +
//            "                        \"    \\\"kubernetes.io/cluster/\$${local.eks_name.value}\\\"   = \\\"shared\\\"\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        86,\n" +
//            "                        \"  })\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        87,\n" +
//            "                        \"}\\n\"\n" +
//            "                    ]\n" +
//            "                ],\n" +
//            "                \"file_path\": \"/test.tf\",\n" +
//            "                \"file_abs_path\": \"/Users/yorhov/development/test.tf\",\n" +
//            "                \"repo_file_path\": \"/test.tf\",\n" +
//            "                \"file_line_range\": [\n" +
//            "                    61,\n" +
//            "                    87\n" +
//            "                ],\n" +
//            "                \"resource\": \"aws_subnet.eks_subnet1\",\n" +
//            "                \"evaluations\": null,\n" +
//            "                \"check_class\": \"checkov.terraform.checks.resource.aws.SubnetPublicIP\",\n" +
//            "                \"fixed_definition\": null,\n" +
//            "                \"entity_tags\": {\n" +
//            "                    \"Name\": \"local.resource_prefix.value-eks-subnet\",\n" +
//            "                    \"kubernetes.io/cluster/local.resource_prefix.value-eks\": \"shared\",\n" +
//            "                    \"git_commit\": \"6e62522d2ab8f63740e53752b84a6e99cd65696a\",\n" +
//            "                    \"git_file\": \"terraform/aws/eks.tf\",\n" +
//            "                    \"git_last_modified_at\": \"2021-05-0211:16:31\",\n" +
//            "                    \"git_last_modified_by\": \"nimrodkor@gmail.com\",\n" +
//            "                    \"git_modifiers\": \"nimrodkor\",\n" +
//            "                    \"git_org\": \"bridgecrewio\",\n" +
//            "                    \"git_repo\": \"terragoat\",\n" +
//            "                    \"kubernetes.io/cluster/$local.resource_prefix.value-eks\": \"shared\",\n" +
//            "                    \"yor_trace\": \"1fb4fa23-a5d6-4d6a-b7dc-88749383f48d\",\n" +
//            "                    \"kubernetes.io/cluster/\$$local.resource_prefix.value-eks\": \"shared\",\n" +
//            "                    \"kubernetes.io/cluster/\$\$$local.resource_prefix.value-eks\": \"shared\"\n" +
//            "                },\n" +
//            "                \"caller_file_path\": null,\n" +
//            "                \"caller_file_line_range\": null,\n" +
//            "                \"breadcrumbs\": {\n" +
//            "                    \"tags\": [\n" +
//            "                        {\n" +
//            "                            \"type\": \"locals\",\n" +
//            "                            \"name\": \"eks_name\",\n" +
//            "                            \"path\": \"/Users/yorhov/development/test.tf\",\n" +
//            "                            \"module_connection\": false\n" +
//            "                        }\n" +
//            "                    ]\n" +
//            "                },\n" +
//            "                \"guideline\": \"https://docs.bridgecrew.io/docs/ensure-vpc-subnets-do-not-assign-public-ip-by-default\"\n" +
//            "            },\n" +
//            "            {\n" +
//            "                \"check_id\": \"CKV_AWS_130\",\n" +
//            "                \"bc_check_id\": \"BC_AWS_NETWORKING_53\",\n" +
//            "                \"check_name\": \"Ensure VPC subnets do not assign public IP by default\",\n" +
//            "                \"check_result\": {\n" +
//            "                    \"result\": \"FAILED\",\n" +
//            "                    \"evaluated_keys\": [\n" +
//            "                        \"map_public_ip_on_launch\"\n" +
//            "                    ]\n" +
//            "                },\n" +
//            "                \"code_block\": [\n" +
//            "                    [\n" +
//            "                        89,\n" +
//            "                        \"resource aws_subnet \\\"eks_subnet2\\\" {\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        90,\n" +
//            "                        \"  vpc_id                  = aws_vpc.eks_vpc.id\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        91,\n" +
//            "                        \"  cidr_block              = \\\"10.10.11.0/24\\\"\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        92,\n" +
//            "                        \"  availability_zone       = \\\"${var .region}b\\\"\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        93,\n" +
//            "                        \"  map_public_ip_on_launch = true\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        94,\n" +
//            "                        \"  tags = merge({\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        95,\n" +
//            "                        \"    Name                                            = \\\"${local.resource_prefix.value}-eks-subnet2\\\"\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        96,\n" +
//            "                        \"    \\\"kubernetes.io/cluster/${local.eks_name.value}\\\" = \\\"shared\\\"\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        97,\n" +
//            "                        \"    }, {\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        98,\n" +
//            "                        \"    git_commit                                       = \\\"6e62522d2ab8f63740e53752b84a6e99cd65696a\\\"\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        99,\n" +
//            "                        \"    git_file                                         = \\\"terraform/aws/eks.tf\\\"\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        100,\n" +
//            "                        \"    git_last_modified_at                             = \\\"2021-05-02 11:16:31\\\"\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        101,\n" +
//            "                        \"    git_last_modified_by                             = \\\"nimrodkor@gmail.com\\\"\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        102,\n" +
//            "                        \"    git_modifiers                                    = \\\"nimrodkor\\\"\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        103,\n" +
//            "                        \"    git_org                                          = \\\"bridgecrewio\\\"\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        104,\n" +
//            "                        \"    git_repo                                         = \\\"terragoat\\\"\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        105,\n" +
//            "                        \"    \\\"kubernetes.io/cluster/\$${local.eks_name.value}\\\" = \\\"shared\\\"\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        106,\n" +
//            "                        \"    yor_trace                                        = \\\"9ce04af2-5321-4e6c-a262-e4d7c1f69525\\\"\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        107,\n" +
//            "                        \"    }, {\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        108,\n" +
//            "                        \"    \\\"kubernetes.io/cluster/\$\$${local.eks_name.value}\\\" = \\\"shared\\\"\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        109,\n" +
//            "                        \"    \\\"kubernetes.io/cluster/\$${local.eks_name.value}\\\"  = \\\"shared\\\"\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        110,\n" +
//            "                        \"    }, {\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        111,\n" +
//            "                        \"    \\\"kubernetes.io/cluster/\$\$\$${local.eks_name.value}\\\" = \\\"shared\\\"\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        112,\n" +
//            "                        \"    \\\"kubernetes.io/cluster/\$\$${local.eks_name.value}\\\"  = \\\"shared\\\"\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        113,\n" +
//            "                        \"    \\\"kubernetes.io/cluster/\$${local.eks_name.value}\\\"   = \\\"shared\\\"\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        114,\n" +
//            "                        \"  })\\n\"\n" +
//            "                    ],\n" +
//            "                    [\n" +
//            "                        115,\n" +
//            "                        \"}\\n\"\n" +
//            "                    ]\n" +
//            "                ],\n" +
//            "                \"file_path\": \"/test.tf\",\n" +
//            "                \"file_abs_path\": \"/Users/yorhov/development/test.tf\",\n" +
//            "                \"repo_file_path\": \"/test.tf\",\n" +
//            "                \"file_line_range\": [\n" +
//            "                    89,\n" +
//            "                    115\n" +
//            "                ],\n" +
//            "                \"resource\": \"aws_subnet.eks_subnet2\",\n" +
//            "                \"evaluations\": null,\n" +
//            "                \"check_class\": \"checkov.terraform.checks.resource.aws.SubnetPublicIP\",\n" +
//            "                \"fixed_definition\": null,\n" +
//            "                \"entity_tags\": {\n" +
//            "                    \"Name\": \"local.resource_prefix.value-eks-subnet2\",\n" +
//            "                    \"kubernetes.io/cluster/local.resource_prefix.value-eks\": \"shared\",\n" +
//            "                    \"git_commit\": \"6e62522d2ab8f63740e53752b84a6e99cd65696a\",\n" +
//            "                    \"git_file\": \"terraform/aws/eks.tf\",\n" +
//            "                    \"git_last_modified_at\": \"2021-05-0211:16:31\",\n" +
//            "                    \"git_last_modified_by\": \"nimrodkor@gmail.com\",\n" +
//            "                    \"git_modifiers\": \"nimrodkor\",\n" +
//            "                    \"git_org\": \"bridgecrewio\",\n" +
//            "                    \"git_repo\": \"terragoat\",\n" +
//            "                    \"kubernetes.io/cluster/$local.resource_prefix.value-eks\": \"shared\",\n" +
//            "                    \"yor_trace\": \"9ce04af2-5321-4e6c-a262-e4d7c1f69525\",\n" +
//            "                    \"kubernetes.io/cluster/\$$local.resource_prefix.value-eks\": \"shared\",\n" +
//            "                    \"kubernetes.io/cluster/\$\$$local.resource_prefix.value-eks\": \"shared\"\n" +
//            "                },\n" +
//            "                \"caller_file_path\": null,\n" +
//            "                \"caller_file_line_range\": null,\n" +
//            "                \"breadcrumbs\": {\n" +
//            "                    \"tags\": [\n" +
//            "                        {\n" +
//            "                            \"type\": \"locals\",\n" +
//            "                            \"name\": \"eks_name\",\n" +
//            "                            \"path\": \"/Users/yorhov/development/test.tf\",\n" +
//            "                            \"module_connection\": false\n" +
//            "                        }\n" +
//            "                    ]\n" +
//            "                },\n" +
//            "                \"guideline\": \"https://docs.bridgecrew.io/docs/ensure-vpc-subnets-do-not-assign-public-ip-by-default\"\n" +
//            "            }"

//    val answer = JSONObject(results)
}
