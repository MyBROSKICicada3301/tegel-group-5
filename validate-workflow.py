#!/usr/bin/env python3
"""
Brand Digital Presence Monitor Workflow Validator
Validates the n8n workflow JSON structure and configuration.
"""

import json
import sys
from pathlib import Path

def validate_workflow(workflow_path):
    """Validate the n8n workflow structure."""
    
    try:
        with open(workflow_path, 'r') as f:
            workflow = json.load(f)
    except json.JSONDecodeError as e:
        print(f"❌ Invalid JSON: {e}")
        return False
    except FileNotFoundError:
        print(f"❌ File not found: {workflow_path}")
        return False
    
    # Check required top-level fields
    required_fields = ['name', 'nodes', 'connections']
    for field in required_fields:
        if field not in workflow:
            print(f"❌ Missing required field: {field}")
            return False
    
    print(f"✅ Workflow name: {workflow['name']}")
    print(f"✅ Number of nodes: {len(workflow['nodes'])}")
    
    # Validate nodes
    node_ids = set()
    node_types = {}
    
    for node in workflow['nodes']:
        # Check required node fields
        if 'id' not in node or 'name' not in node or 'type' not in node:
            print(f"❌ Node missing required fields: {node}")
            return False
        
        node_id = node['id']
        node_type = node['type']
        
        if node_id in node_ids:
            print(f"❌ Duplicate node ID: {node_id}")
            return False
        
        node_ids.add(node_id)
        node_types[node_id] = node_type
        print(f"  ✓ Node: {node['name']} ({node_type})")
    
    # Validate connections
    if 'connections' in workflow:
        for source_node, connections in workflow['connections'].items():
            if source_node not in node_ids:
                print(f"❌ Connection references unknown node: {source_node}")
                return False
            
            for connection_type, connection_list in connections.items():
                for connection_group in connection_list:
                    for connection in connection_group:
                        if 'node' not in connection:
                            print(f"❌ Invalid connection format: {connection}")
                            return False
                        
                        target_node = connection['node']
                        if target_node not in node_ids:
                            print(f"❌ Connection references unknown target node: {target_node}")
                            return False
    
    # Check for required node types
    required_node_types = [
        'n8n-nodes-base.cron',  # Scheduler
        'n8n-nodes-base.set',   # Variables
        'n8n-nodes-base.switch', # Router
        'n8n-nodes-base.httpRequest', # Data collection
        'n8n-nodes-base.code',  # Processing
        'n8n-nodes-base.notion' # Storage
    ]
    
    found_types = set(node_types.values())
    for required_type in required_node_types:
        if required_type not in found_types:
            print(f"⚠️  Missing recommended node type: {required_type}")
    
    # Platform coverage check
    platforms = ['instagram', 'facebook', 'twitter', 'linkedin', 'tiktok', 'youtube']
    platform_nodes = [node for node in workflow['nodes'] if any(platform in node['name'].lower() for platform in platforms)]
    
    print(f"✅ Platform coverage: {len(platform_nodes)} platform-specific nodes found")
    
    # Check for AI analysis
    ai_nodes = [node for node in workflow['nodes'] if 'openai' in node.get('type', '').lower() or 'ai' in node['name'].lower()]
    if ai_nodes:
        print(f"✅ AI analysis: {len(ai_nodes)} AI-related nodes found")
    else:
        print("⚠️  No AI analysis nodes found")
    
    # Check for error handling
    error_nodes = [node for node in workflow['nodes'] if 'error' in node['name'].lower() or 'log' in node['name'].lower()]
    if error_nodes:
        print(f"✅ Error handling: {len(error_nodes)} error handling nodes found")
    else:
        print("⚠️  No error handling nodes found")
    
    print("✅ Workflow validation completed successfully!")
    return True

def validate_config_template(config_path):
    """Validate the configuration template structure."""
    
    try:
        with open(config_path, 'r') as f:
            config = json.load(f)
    except json.JSONDecodeError as e:
        print(f"❌ Invalid JSON in config template: {e}")
        return False
    except FileNotFoundError:
        print(f"❌ Config template not found: {config_path}")
        return False
    
    # Check required sections
    required_sections = [
        'workflow_configuration',
        'notion_database_templates',
        'api_requirements',
        'deployment_checklist'
    ]
    
    for section in required_sections:
        if section not in config:
            print(f"❌ Missing required section in config template: {section}")
            return False
    
    print("✅ Configuration template validation completed successfully!")
    return True

def main():
    """Main validation function."""
    
    print("🔍 Brand Digital Presence Monitor - Workflow Validator")
    print("=" * 60)
    
    # Validate workflow JSON
    workflow_path = Path("brand-digital-presence-monitor-workflow.json")
    if workflow_path.exists():
        print("\n📋 Validating workflow JSON...")
        workflow_valid = validate_workflow(workflow_path)
    else:
        print(f"❌ Workflow file not found: {workflow_path}")
        workflow_valid = False
    
    # Validate configuration template
    config_path = Path("brand-monitor-config-template.json")
    if config_path.exists():
        print("\n⚙️  Validating configuration template...")
        config_valid = validate_config_template(config_path)
    else:
        print(f"❌ Configuration template not found: {config_path}")
        config_valid = False
    
    # Check documentation files
    docs = [
        "brand-monitor-configuration-guide.md",
        "README-brand-monitor.md"
    ]
    
    print("\n📚 Checking documentation files...")
    docs_valid = True
    for doc in docs:
        if Path(doc).exists():
            print(f"✅ Documentation found: {doc}")
        else:
            print(f"❌ Documentation missing: {doc}")
            docs_valid = False
    
    # Final validation summary
    print("\n" + "=" * 60)
    print("🎯 VALIDATION SUMMARY")
    print("=" * 60)
    
    all_valid = workflow_valid and config_valid and docs_valid
    
    if all_valid:
        print("🎉 All validations passed! The Brand Digital Presence Monitor is ready for deployment.")
        print("\n📝 Next steps:")
        print("1. Import the workflow JSON into your n8n instance")
        print("2. Configure API credentials for all platforms")
        print("3. Set up Notion databases using the provided templates")
        print("4. Customize the workflow variables for your brand")
        print("5. Test individual nodes before activating the schedule")
        return 0
    else:
        print("❌ Some validations failed. Please review the errors above.")
        return 1

if __name__ == "__main__":
    sys.exit(main())